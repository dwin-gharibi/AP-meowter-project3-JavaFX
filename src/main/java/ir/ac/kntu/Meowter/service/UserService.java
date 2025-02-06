package ir.ac.kntu.Meowter.service;

import ir.ac.kntu.Meowter.model.*;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserService {

    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public boolean createUser(String username, String email, String password, Role role) {

        try{
            ValidationUtil.validateUsername(username);
            ValidationUtil.validateEmail(email);
            ValidationUtil.validatePassword(password);
        } catch (Exception e) {
            System.out.println(CliFormatter.boldRed(e.getMessage()));
            return false;
        }

        User user = new User(username, email, password, role);
        userRepository.save(user);
        return true;
    }

    public User updateUsername(User user, String newUsername) {
        try{
            ValidationUtil.validateUsername(newUsername);
            user.setUsername(newUsername);
            userRepository.update(user);
            SessionManager.saveSession(user);
            CliFormatter.printTypingEffect(CliFormatter.boldGreen("Username updated successfully."));
        } catch (Exception e) {
            System.out.println(CliFormatter.boldRed(e.getMessage()));
        }
        return user;
    }

    public User updatePassword(User user, String newPassword) {
        try {
            ValidationUtil.validatePassword(newPassword);

            user.setPassword(newPassword);
            userRepository.update(user);
            SessionManager.saveSession(user);
            CliFormatter.printTypingEffect(CliFormatter.boldGreen("Password updated successfully."));
        } catch (Exception e) {
            System.out.println(CliFormatter.boldRed(e.getMessage()));
        }

        return user;
    }

    public User updateBio(User user, String newBio) {
        user.setBio(newBio);
        userRepository.update(user);
        SessionManager.saveSession(user);

        return user;
    }

    public void setLabels(User user, String labels) {

        user.setUser_labels(Collections.emptySet());
        List<String> labelList = Arrays.asList(labels.split(","));
        for (String label : labelList) {
            user.addLabel(PostLabel.valueOf(label));
        }
        userRepository.update(user);
        SessionManager.saveSession(user);

    }

    public void setDepartments(User user, String departments) {

        user.setDepartments(Collections.emptySet());
        List<String> departmentlist = Arrays.asList(departments.split(","));
        for (String department : departmentlist) {
            user.addLabel(PostLabel.valueOf(department));
        }
        userRepository.update(user);
        SessionManager.saveSession(user);

    }

    public User updateDateOfBirth(User user, LocalDateTime dateOfBirth) {
        user.setDateofbirth(dateOfBirth);
        userRepository.update(user);
        SessionManager.saveSession(user);

        return user;
    }

    public User updatePrivacySetting(User user, boolean isPrivate) {
        user.setPrivate(isPrivate);
        userRepository.update(user);
        SessionManager.saveSession(user);
        return user;
    }

    public User loginWithUsername(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User loginWithEmail(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User register(String username, String email, String password) {

        ValidationUtil.validateUsername(username);
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);


        if (userRepository.existsByEmail(email)) {
            return null;
        }

        User newUser = new User(username, email, password, Role.USER);
        userRepository.save(newUser);
        return newUser;
    }

    public void removeFollower(User loggedInUser, User follower) {
        if (loggedInUser.getFollowers().contains(follower)) {
            loggedInUser.getFollowers().remove(follower);
            follower.getFollowing().remove(loggedInUser);
            userRepository.update(loggedInUser);
            userRepository.update(follower);
            System.out.println("Removed follower: @" + follower.getUsername());
        } else {
            System.out.println("Follower not found.");
        }
    }

    public void unfollowUser(User loggedInUser, User userToUnfollow) {
        if (loggedInUser.getFollowing().contains(userToUnfollow)) {
            loggedInUser.getFollowing().remove(userToUnfollow);
            userToUnfollow.getFollowers().remove(loggedInUser);
            userRepository.update(loggedInUser);
            userRepository.update(userToUnfollow);
            System.out.println("Unfollowed: @" + userToUnfollow.getUsername());
        } else {
            System.out.println("You are not following @" + userToUnfollow.getUsername());
        }
    }

    public User searchUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<FollowRequest> getFollowRequests(User user) {
        return userRepository.getFollowRequests(user);
    }


    public void sendFollowRequest(User loggedInUser, User recipientUser) {
        FollowRequest existingRequest = userRepository.findFollowRequest(loggedInUser, recipientUser);
        if (existingRequest != null) {
            System.out.println("Follow request already exists.");
            return;
        }

        FollowRequest followRequest = new FollowRequest(loggedInUser, recipientUser);

        if (recipientUser.isPrivate()) {
            userRepository.saveFollowRequest(followRequest);
            System.out.println("Follow request sent to @" + recipientUser.getUsername());
        } else {
            acceptFollowRequest(loggedInUser, followRequest);
            System.out.println("Follow request automatically accepted for public profile of @" + recipientUser.getUsername());
        }
    }

    public void acceptFollowRequest(User loggedInUser, FollowRequest followRequest) {
        followRequest.setStatus(FollowRequestStatus.ACCEPTED);
        userRepository.updateFollowRequest(followRequest);

        loggedInUser.followUser(followRequest.getRecipient());
        userRepository.update(loggedInUser);
        userRepository.update(followRequest.getRecipient());
        System.out.println("Follow request accepted successfully.");
    }


    public void rejectFollowRequest(User loggedInUser, FollowRequest followRequest) {
        followRequest.setStatus(FollowRequestStatus.REJECTED);
        userRepository.updateFollowRequest(followRequest);
        System.out.println("Follow request rejected.");
    }


    public void viewAllUsers() {
        for (User user : userRepository.findAll()) {
            System.out.println("User: " + user.getUsername());
        }
    }
}

