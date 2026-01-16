create table users (
    id varchar(64) primary key,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    password_salt varchar(255) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create unique index ux_users_email on users(email);

create table refresh_tokens (
    token varchar(255) primary key,
    user_id varchar(64) not null,
    expires_at timestamp not null,
    created_at timestamp not null
);

create index idx_refresh_tokens_user_id on refresh_tokens(user_id);

create table projects (
    id varchar(64) primary key,
    owner_user_id varchar(64) not null,
    title varchar(255) not null,
    status varchar(32) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_projects_owner_user_id on projects(owner_user_id);

create table musics (
    id varchar(64) primary key,
    project_id varchar(64) not null,
    alias varchar(255),
    status varchar(32) not null,
    requested_config text,
    last_operation varchar(32),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_musics_project_id on musics(project_id);

create table music_versions (
    id varchar(64) primary key,
    music_id varchar(64) not null,
    status varchar(32) not null,
    config text not null,
    file_id varchar(64),
    duration_seconds integer not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_music_versions_music_id on music_versions(music_id);

create table music_generation_tasks (
    id varchar(64) primary key,
    music_id varchar(64) not null,
    external_id varchar(128) not null,
    status varchar(32) not null,
    provider varchar(64) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    error_message text
);

create unique index ux_music_tasks_provider_external_id on music_generation_tasks(provider, external_id);
create index idx_music_tasks_music_id on music_generation_tasks(music_id);
create index idx_music_tasks_status_provider_updated_at on music_generation_tasks(status, provider, updated_at);

create table images (
    id varchar(64) primary key,
    project_id varchar(64) not null,
    status varchar(32) not null,
    requested_config text,
    last_operation varchar(32),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_images_project_id on images(project_id);

create table image_versions (
    id varchar(64) primary key,
    image_id varchar(64) not null,
    status varchar(32) not null,
    config text not null,
    file_id varchar(64),
    created_at timestamp not null,
    updated_at timestamp not null
);

create index idx_image_versions_image_id on image_versions(image_id);

create table image_generation_tasks (
    id varchar(64) primary key,
    image_id varchar(64) not null,
    external_id varchar(128) not null,
    status varchar(32) not null,
    provider varchar(64) not null,
    poll_count integer not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    error_message text
);

create unique index ux_image_tasks_provider_external_id on image_generation_tasks(provider, external_id);
create index idx_image_tasks_image_id on image_generation_tasks(image_id);
create index idx_image_tasks_status_provider_updated_at on image_generation_tasks(status, provider, updated_at);

create table audio_files (
    id varchar(64) primary key,
    path text not null
);

create table image_files (
    id varchar(64) primary key,
    path text not null
);

create table video_files (
    id varchar(64) primary key,
    path text not null
);

create table videos (
    id varchar(64) primary key,
    project_id varchar(64) not null,
    status varchar(32) not null,
    file_id varchar(64),
    segments_json text not null,
    image_groups_json text not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create unique index ux_videos_project_id on videos(project_id);
