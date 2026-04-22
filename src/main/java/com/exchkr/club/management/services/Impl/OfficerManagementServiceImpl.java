package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.*;
import com.exchkr.club.management.model.entity.*;
import com.exchkr.club.management.model.api.request.MemberRequest;
import com.exchkr.club.management.model.api.response.MemberCountResponse;
import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.services.OfficerManagementService;
import com.exchkr.club.management.services.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OfficerManagementServiceImpl implements OfficerManagementService {

	private final UserRepository userRepository;
	private final UserClubRepository userClubRepository;
	private final ClubRepository clubRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	public OfficerManagementServiceImpl(UserRepository userRepository, UserClubRepository userClubRepository,
			ClubRepository clubRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
		this.userRepository = userRepository;
		this.userClubRepository = userClubRepository;
		this.clubRepository = clubRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	@Override
	public List<UserDTO> getClubMembers(Long clubId, String filter) {
		String clubName = clubRepository.findById(clubId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found")).getClubName();

		return userClubRepository
				.findAllMembersByClubId(clubId).stream().map(m -> UserDTO.fromUser(m.getUser(),
						Collections.singletonList(m.getRoleName()), clubName, clubId, m.getJoinedAt()))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void addMember(MemberRequest request, Long actingUserId, Long clubId) {
		// 1. Check if user exists
		Optional<User> existingUser = userRepository.findUserByEmail(request.getEmail());

		User member;
		String rawPassword = null;

		if (existingUser.isEmpty()) {
			// It's a brand new user: generate a temporary plain-text password
			rawPassword = UUID.randomUUID().toString().substring(0, 12);
			member = createNewBaseUser(request, rawPassword);
		} else {
			member = existingUser.get();
		}

		// 2. Membership Check
		if (userClubRepository.existsByUserIdAndClubId(member.getUserId(), clubId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this club.");
		}

		// 3. Mapping: Link User to Club
		UserClub membership = new UserClub();
		membership.setUserId(member.getUserId());
		membership.setClubId(clubId);
		membership.setRoleId(request.getRoleId() != null ? request.getRoleId() : 6L);
		membership.setStatus("Active");
		membership.setJoinedAt(Instant.now());

		userClubRepository.save(membership);

		// 4. Notification
		// Pass 'rawPassword' (the plain text) to the email service.
		// If it's null (existing user), the email service handles the alternate
		// message.
		emailService.sendInitialCredentials(member.getEmail(), rawPassword, "Welcome to the club!");
	}

	/**
	 * Helper to create a new User. Takes rawPassword so it can be encoded for DB
	 * and kept plain for the email.
	 */
	private User createNewBaseUser(MemberRequest request, String rawPassword) {
		User user = new User();
		user.setEmail(request.getEmail());
		user.setFullName(request.getFullName());

		String[] nameParts = request.getFullName().trim().split("\\s+", 2);
		user.setFirstName(nameParts[0]);
		user.setLastName(nameParts.length > 1 ? nameParts[1] : "");
		user.setUserName(request.getEmail());

		// We encode the password for the database here
		user.setPassword(passwordEncoder.encode(rawPassword));

		user.setStatus("Active");
		user.setCreatedOn(Instant.now());
		user.setLastUpdatedOn(Instant.now());

		return userRepository.save(user);
	}

	@Override
	public MemberCountResponse getMemberCounts(Long clubId) {
		// 1. Get Total Active Count
		long totalCount = userClubRepository.countByClubIdAndStatus(clubId, "Active");

		// 2. Get Count for "This Month"
		// Calculate the Instant representing the start of the current month
		java.time.ZonedDateTime startOfMonth = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).withDayOfMonth(1)
				.withHour(0).withMinute(0).withSecond(0).withNano(0);

		Instant since = startOfMonth.toInstant();

		long joinedThisMonth = userClubRepository.countByClubIdAndStatusAndJoinedAtAfter(clubId, "Active", since);

		return new MemberCountResponse(totalCount, joinedThisMonth);
	}

	@Override
	@Transactional
	public void removeMember(Long memberId, Long clubId) {
		// Soft delete the membership mapping only
		userClubRepository.deactivateMembership(memberId, clubId);
	}

	@Override
	public void addMembersCSV(MultipartFile membersCsvFile, Long actingUserId, Long clubId) {

		if (membersCsvFile == null || membersCsvFile.isEmpty()) {
			throw new IllegalArgumentException("CSV file is required and cannot be empty");
		}

		if (membersCsvFile.getSize() > 1 * 1024 * 1024) {
			throw new IllegalArgumentException("File size exceeds 1MB limit");
		}

		String filename = membersCsvFile.getOriginalFilename();
		if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
			throw new IllegalArgumentException("Only CSV files are allowed");
		}

		List<MemberRequest> validRequests = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(membersCsvFile.getInputStream(), StandardCharsets.UTF_8))) {

			String line;
			int rowNumber = 0;
			boolean isHeader = true;

			while ((line = reader.readLine()) != null) {
				rowNumber++;

				if (rowNumber > 10_000) {
					throw new IllegalArgumentException("CSV exceeds maximum 10,000 rows");
				}

				if (isHeader) {
					isHeader = false;
					continue;
				}

				if (line.trim().isEmpty()) {
					continue;
				}

				List<String> columns = parseCsvLine(line);
				if (columns.size() < 2) {
					System.err.println("Row " + rowNumber + ": Invalid format, skipping");
					continue;
				}

				String fullName = columns.get(0);
				String email = columns.get(1);

				if (!isValidName(fullName) || !isValidEmail(email)) {
					System.err.println("Row " + rowNumber + ": Validation failed, skipping");
					continue;
				}

				MemberRequest request = new MemberRequest();
				request.setFullName(fullName);
				request.setEmail(email);
				request.setRoleId(6L); // default role

				validRequests.add(request);
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to read CSV file", e);
		}

		// ASYNC bulk insert (non-blocking)
		addMultipleMembers(validRequests, actingUserId, clubId);
	}

	private List<String> parseCsvLine(String line) {
		List<String> columns = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (char c : line.toCharArray()) {
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				columns.add(current.toString().replace("\"", "").trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		columns.add(current.toString().replace("\"", "").trim());
		return columns;
	}

	private boolean isValidName(String name) {
		return name != null && !name.isBlank() && name.length() <= 100 && name.matches("^[a-zA-Z\\s'-]+$")
				&& !name.matches("^[=+\\-@\\t\\r].*");
	}

	private boolean isValidEmail(String email) {
		return email != null && email.length() <= 255
				&& email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
				&& !email.matches("^[=+\\-@\\t\\r].*");
	}

	@Async("bulkMemberExecutor")
	public CompletableFuture<Void> addMultipleMembers(List<MemberRequest> requests, Long actingUserId, Long clubId) {

		for (MemberRequest request : requests) {
			try {
				addSingleMemberInNewTransaction(request, actingUserId, clubId);
			} catch (Exception ex) {
				System.err.println("Failed to add member: " + request.getEmail());
				ex.printStackTrace();
			}
		}

		return CompletableFuture.completedFuture(null);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void addSingleMemberInNewTransaction(MemberRequest request, Long actingUserId, Long clubId) {

		Optional<User> existingUser = userRepository.findUserByEmail(request.getEmail());

		User member;
		String rawPassword = null;

		if (existingUser.isEmpty()) {
			rawPassword = UUID.randomUUID().toString().substring(0, 12);
			member = createNewBaseUser(request, rawPassword);
		} else {
			member = existingUser.get();
		}

		if (userClubRepository.existsByUserIdAndClubId(member.getUserId(), clubId)) {
			System.err.println("User already in club: " + member.getEmail());
			return;
		}

		UserClub membership = new UserClub();
		membership.setUserId(member.getUserId());
		membership.setClubId(clubId);
		membership.setRoleId(request.getRoleId() != null ? request.getRoleId() : 6L);
		membership.setStatus("Active");
		membership.setJoinedAt(Instant.now());

		userClubRepository.save(membership);

		emailService.sendMembersCredentials(member.getEmail(), rawPassword, "Welcome to the club!");
	}
}