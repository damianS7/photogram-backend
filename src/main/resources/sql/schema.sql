DROP SCHEMA IF EXISTS public CASCADE;

CREATE SCHEMA public AUTHORIZATION pg_database_owner;

COMMENT ON SCHEMA public IS 'standard public schema';


CREATE TYPE public."customer_role_type" AS ENUM (
	'CUSTOMER',
	'ADMIN'
);

CREATE CAST (varchar as customer_role_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customers (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	email varchar(80) NOT NULL,
	"role" public."customer_role_type" DEFAULT 'CUSTOMER'::customer_role_type NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_email_key UNIQUE (email),
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);

CREATE TYPE public."customer_gender_type" AS ENUM (
	'MALE',
	'FEMALE'
);

CREATE CAST (varchar as customer_gender_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_profiles (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	username varchar(20) NOT NULL,
	first_name varchar(20) NOT NULL,
	last_name varchar(40) NOT NULL,
	phone varchar(14) NOT NULL,
	birthdate date NOT NULL,
	gender public."customer_gender_type" NOT NULL,
	image_filename varchar(100) NULL,
	about_me varchar(255) NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."account_status_type" AS ENUM (
	'PENDING_VERIFICATION',
	'SUSPENDED',
	'ACTIVE'
);

CREATE CAST (varchar as account_status_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_auth (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	password_hash varchar(60) NOT NULL,
	account_status public."account_status_type" DEFAULT 'ENABLED'::account_status_type NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."auth_token_type" AS ENUM (
	'ACCOUNT_ACTIVATION',
	'RESET_PASSWORD'
);

CREATE CAST (varchar as auth_token_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_auth_tokens (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	token varchar(100) NOT NULL,
	used BOOLEAN DEFAULT FALSE,
	type public."auth_token_type" NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	expires_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT auth_token_pkey PRIMARY KEY (id),
	CONSTRAINT unique_customer_type_token UNIQUE (customer_id, type),
	CONSTRAINT auth_token_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_follows (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    followed_customer_id int4 NOT NULL,
    follower_customer_id int4 NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT customer_followers_pkey PRIMARY KEY (id),
    CONSTRAINT unique_customer_follower UNIQUE (followed_customer_id, follower_customer_id),
    CONSTRAINT followed_customer_id_fkey FOREIGN KEY (followed_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE,
    CONSTRAINT follower_customer_id_fkey FOREIGN KEY (follower_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_posts (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id int4 NOT NULL,
    photo_filename varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT customer_posts_pkey PRIMARY KEY (id),
    CONSTRAINT customer_posts_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_post_likes (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    post_id int4 NOT NULL,
    customer_id int4 NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT customer_post_like_pkey PRIMARY KEY (id),
    CONSTRAINT unique_customer_post_like UNIQUE (post_id, customer_id),
    CONSTRAINT post_id_fkey FOREIGN KEY (post_id) REFERENCES public.customer_posts(id) ON DELETE CASCADE,
    CONSTRAINT customer_post_like_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_post_comments (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    post_id int4 NOT NULL,
    customer_id int4 NOT NULL,
    comment varchar(255) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT customer_post_comment_pkey PRIMARY KEY (id),
    CONSTRAINT customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE,
    CONSTRAINT post_id_fkey FOREIGN KEY (post_id) REFERENCES public.customer_posts(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_settings (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	key varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    CONSTRAINT settings_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE,
	CONSTRAINT settings_pkey PRIMARY KEY (id)
);