drop table if exists message;

create table message (
	message_id int not null auto_increment,
	message text not null,
	create_time timestamp not null default now(),
	lock_time datetime,
	state enum('P', 'A') not null default 'P',
	attempts int not null default 0,
	primary key (message_id)
)
ENGINE = InnoDB;
