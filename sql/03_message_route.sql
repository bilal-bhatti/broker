drop table if exists message_route;

create table message_route (
	route_id int not null auto_increment,
	topic varchar(16) not null,
	consumer varchar(512) not null,
	create_time timestamp not null default now(),
	primary key (route_id)
)
ENGINE = InnoDB;

alter table message_route add unique index(topic, consumer);


insert into message_route (topic, consumer) values ('test', 'http://localhost:8080/broker/test');
insert into message_route (topic, consumer) values ('reminder', 'http://localhost:8080/scuid/callback');
insert into message_route (topic, consumer) values ('notification', 'http://localhost:8080/scuid/callback');
