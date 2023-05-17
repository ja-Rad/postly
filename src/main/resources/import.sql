--users table
INSERT INTO users (password, email, enabled, active_profile) VALUES ('$10$M1i09dbTWf34HKSsB6kA/esPjn3SiS4xR1udHXfEH0GONHkPwKJR6', 'kalmanjudin2000@gmail.com', 1, 1);
INSERT INTO users (password, email, enabled, active_profile) VALUES ('$10$M1i09dbTWf34HKSsB6kA/esPjn3SiS4xR1udHXfEH0GONHkPwKJR6', 'test1@gmail.com', 1, 1);
INSERT INTO users (password, email, enabled, active_profile) VALUES ('$10$M1i09dbTWf34HKSsB6kA/esPjn3SiS4xR1udHXfEH0GONHkPwKJR6', 'test2@gmail.com', 1, 1);
INSERT INTO users (password, email, enabled, active_profile) VALUES ('$10$M1i09dbTWf34HKSsB6kA/esPjn3SiS4xR1udHXfEH0GONHkPwKJR6', 'test3@gmail.com', 1, 1);
INSERT INTO users (password, email, enabled, active_profile) VALUES ('$10$M1i09dbTWf34HKSsB6kA/esPjn3SiS4xR1udHXfEH0GONHkPwKJR6', 'test4@gmail.com', 1, 1);


--profiles table
INSERT INTO profiles (profile_id, creation_date, username) VALUES (1, NOW(), 'rad');
INSERT INTO profiles (profile_id, creation_date, username) VALUES (2, NOW(), 'test1-profile');
INSERT INTO profiles (profile_id, creation_date, username) VALUES (3, NOW(), 'test2-profile');
INSERT INTO profiles (profile_id, creation_date, username) VALUES (4, NOW(), 'test3-profile');
INSERT INTO profiles (profile_id, creation_date, username) VALUES (5, NOW(), 'test4-profile');


-- posts table
INSERT INTO posts (creation_date, title, `description`, profile_id) VALUES (NOW(), 'rad1 Post Title', 'rad1 Post Desc', 1);
INSERT INTO posts (creation_date, title, `description`, profile_id) VALUES (NOW(), 'test1 Post Title', 'test1 Post Desc', 2);
INSERT INTO posts (creation_date, title, `description`, profile_id) VALUES (NOW(), 'test2 Post Title', 'test2 Post Desc', 3);
INSERT INTO posts (creation_date, title, `description`, profile_id) VALUES (NOW(), 'test3 Post Title', 'test3 Post Desc', 4);
INSERT INTO posts (creation_date, title, `description`, profile_id) VALUES (NOW(), 'test4 Post Title', 'test4 Post Desc', 5);


-- comments table
INSERT INTO comments (creation_date, `description`, post_id, profile_id) VALUES (NOW(), 'Post 1 Profile 1', 1, 1);
INSERT INTO comments (creation_date, `description`, post_id, profile_id) VALUES (NOW(), 'Post 1 Profile 2', 1, 2);
INSERT INTO comments (creation_date, `description`, post_id, profile_id) VALUES (NOW(), 'Post 2 Profile 1', 2, 1);
INSERT INTO comments (creation_date, `description`, post_id, profile_id) VALUES (NOW(), 'Post 3 Profile 4', 3, 4);
INSERT INTO comments (creation_date, `description`, post_id, profile_id) VALUES (NOW(), 'Post 5 Profile 4', 5, 4);


-- posts table





