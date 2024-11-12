drop database if exists chatbot;
create database chatbot character set utf8mb4 collate utf8mb4_0900_ai_ci;

create table chatbot.sched_status
(
  id        tinyint unsigned auto_increment,
  name      varchar(20) not null,
  insert_ts timestamp default current_timestamp not null,
  constraint sched_status_pk primary key (id)
) engine = innodb;

insert into chatbot.sched_status (name)
values ('SCHEDULED'),
       ('COMPLETED');

create table chatbot.visitor_conv_sched
(
  id int unsigned not null,
  sched_status_id tinyint unsigned not null,
  event_ts timestamp default current_timestamp not null,
  constraint visitor_conv_sched_pk primary key (id),
  constraint fk_visitor_conv_sched_id foreign key (id) references chatbot.visitor_conv(id),
  constraint fk_sched_status_id foreign key (sched_status_id) references chatbot.sched_status(id)
) engine = innodb;

create index vc_sched_status_id_idx on chatbot.visitor_conv_sched (sched_status_id);