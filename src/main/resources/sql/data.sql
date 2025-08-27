INSERT INTO public.customers (email,"role",created_at,updated_at) VALUES
	 ('damian@demo.com','ADMIN'::public."customer_role_type",'2025-04-17 02:07:41.382291','2025-04-17 02:07:41.382291'),
	 ('alice@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('david@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('alexa@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('alana@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('robert@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('angela@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('thomas@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616'),
	 ('michael@demo.com','CUSTOMER'::public."customer_role_type",'2025-04-06 02:42:27.378616','2025-04-06 02:42:27.378616');
INSERT INTO public.customer_profiles (customer_id, username,first_name,last_name,phone,birthdate,gender,image_filename, about_me,updated_at) VALUES
	 (1, 'DamianS7','Damian','J.','701 444 113','1987-06-02','FEMALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:47:10.771533'),
	 (2, 'AliceWhite','Alice','White','701 444 113','1987-06-02','FEMALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:47:10.771533'),
	 (3, 'David_B1', 'David','Brown','901 322 223','1993-07-04','MALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (4, 'Alxa1','Alexa','Brown','901 322 223','1993-07-04','FEMALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (5, 'Alana81','Alana','Brown','901 322 223','1993-07-04','FEMALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (6, 'Robert.Brown','Robert','Brown','901 322 223','1993-07-04','MALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (7, 'Angela_SX','Angela','Brown','901 322 223','1993-07-04','FEMALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (8, 'Thom.b4','Thomas','Brown','901 322 223','1993-07-04','MALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419'),
	 (9, 'MLRX5','Michael','Brown','901 322 223','1993-07-04','MALE'::public."customer_gender_type",NULL,NULL,'2025-04-28 01:48:28.903419');
INSERT INTO public.customer_auth (customer_id,password_hash,account_status,updated_at) VALUES
	 (1,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','VERIFIED'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (2,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (3,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (4,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (5,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (6,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (7,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (8,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439'),
	 (9,'$2a$10$hyxP/Azy1W1OjjhRarmDzO3J.CcMc5n1D4UzQJKUD4YD/yPV4AL06','PENDING_VERIFICATION'::public."account_status_type",'2025-04-28 13:11:10.477439');
INSERT INTO public.customer_follows (followed_customer_id,follower_customer_id,created_at) VALUES
	 (1, 2,'2025-04-14 00:24:39.778237'),
	 (1, 3,'2025-04-14 00:24:39.778237'),
	 (1, 4,'2025-04-14 00:24:39.778237'),
	 (1, 5,'2025-04-14 00:24:39.778237');
INSERT INTO public.customer_settings (customer_id,setting_key,setting_value) VALUES
(1, 'lang','es'),
(2, 'lang','es'),
(3, 'lang','es'),
(4, 'lang','es'),
(5, 'lang','es'),
(6, 'lang','es'),
(7, 'lang','es'),
(8, 'lang','es'),
(9, 'lang','es');