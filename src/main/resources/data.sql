-- application.yml 파일의 jpa: defer-datasource-initialization: true로 설정되어있어야 데이터 insert 수행.
-- (테이블의 생성시점 이후에 데이터 insert가 발생할 수 있도록 함.)

insert into account_user(id, name, created_at, updated_at)
values(1, 'Pororo', now(), now());
insert into account_user(id, name, created_at, updated_at)
values(2, 'Lupi', now(), now());
insert into account_user(id, name, created_at, updated_at)
values(3, 'Eddie', now(), now());
