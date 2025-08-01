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
	first_name varchar(20) NOT NULL,
	last_name varchar(40) NOT NULL,
	phone varchar(14) NOT NULL,
	birthdate date NOT NULL,
	gender public."customer_gender_type" NOT NULL,
	avatar_filename varchar(100) NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT profiles_customer_id_key UNIQUE (customer_id),
	CONSTRAINT profiles_pkey PRIMARY KEY (id),
	CONSTRAINT profiles_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."auth_status_type" AS ENUM (
	'DISABLED',
	'ENABLED'
);

CREATE CAST (varchar as auth_status_type) WITH INOUT AS IMPLICIT;

CREATE TYPE public."email_verification_status_type" AS ENUM (
	'NOT_VERIFIED',
	'VERIFIED'
);

CREATE CAST (varchar as email_verification_status_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.customer_auth (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	password_hash varchar(60) NOT NULL,
	auth_account_status public."auth_status_type" DEFAULT 'ENABLED'::auth_status_type NOT NULL,
	"email_verification_status" public."email_verification_status_type" DEFAULT 'NOT_VERIFIED'::email_verification_status_type NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT auth_customer_id_key UNIQUE (customer_id),
	CONSTRAINT auth_pkey PRIMARY KEY (id),
	CONSTRAINT auth_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.customer_friends (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    customer_id int4 NOT NULL,
    friend_customer_id int4 NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT unique_customer_friend UNIQUE (customer_id, friend_customer_id),
    CONSTRAINT customer_friends_pkey PRIMARY KEY (id),
    CONSTRAINT customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE,
    CONSTRAINT friend_customer_id_fkey FOREIGN KEY (friend_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.groups (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    owner_customer_id int4 NOT NULL,
    name varchar(30) NOT NULL,
    description varchar(220) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT group_name_unique UNIQUE (name),
    CONSTRAINT groups_pkey PRIMARY KEY (id),
    CONSTRAINT owner_customer_id_fkey FOREIGN KEY (owner_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TYPE public."group_member_role_type" AS ENUM (
	'OWNER',
	'MEMBER'
);

CREATE CAST (varchar as group_member_role_type) WITH INOUT AS IMPLICIT;

CREATE TABLE public.group_members (
    id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
    group_id int4 NOT NULL,
    member_customer_id int4 NOT NULL,
    member_role public."group_member_role_type" DEFAULT 'MEMBER'::group_member_role_type NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT group_member_unique UNIQUE (group_id, member_customer_id),
    CONSTRAINT group_members_pkey PRIMARY KEY (id),
    CONSTRAINT group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE,
    CONSTRAINT member_customer_id_fkey FOREIGN KEY (member_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.group_expenses (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	group_id int4 NOT NULL,
	payer_customer_id int4 NULL,
	description text NOT NULL,
	amount numeric(15, 2) NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT customers_expenses_pkey PRIMARY KEY (id),
	CONSTRAINT group_id_fkey FOREIGN KEY (group_id) REFERENCES public.groups(id) ON DELETE CASCADE,
	CONSTRAINT customer_payer_customer_id_fkey FOREIGN KEY (payer_customer_id) REFERENCES public.customers(id) ON DELETE CASCADE
);

CREATE TABLE public.settings (
	id int4 GENERATED ALWAYS AS IDENTITY NOT NULL,
	customer_id int4 NOT NULL,
	key varchar(255) NOT NULL,
    value varchar(255) NOT NULL,
    CONSTRAINT settings_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(id) ON DELETE CASCADE,
	CONSTRAINT settings_pkey PRIMARY KEY (id)
);