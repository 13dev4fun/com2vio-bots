create table if not exists file_exclude
(
    id           int auto_increment
    primary key,
    file_path    varchar(512) not null comment '忽略的文件路径',
    repo_uuid    char(36)     not null comment '仓库id',
    create_time  datetime     not null comment '创建时间',
    account_uuid char(36)     not null comment '设置这一忽略文件路径的账户id'
    )
    comment '对用户指定路径文件的忽略' engine = MyISAM;

create table if not exists issue
(
    id                int auto_increment
    primary key,
    type              varchar(256)                  null,
    tool              varchar(45)                   null,
    start_commit      varchar(64)                   null,
    start_commit_date datetime                      null,
    end_commit        varchar(64)                   null,
    end_commit_date   datetime                      null,
    repo_uuid         varchar(36)                   null,
    file_name         varchar(512)                  null,
    create_time       datetime                      null,
    update_time       datetime                      null,
    priority          tinyint                       null,
    status            varchar(20)                   null,
    manual_status     varchar(45) default 'Default' null,
    resolution        varchar(20)                   null,
    issue_category    varchar(50)                   null,
    producer          varchar(64)                   null,
    producer_status   varchar(20) default 'Default' null,
    solver            varchar(64)                   null,
    solve_commit      varchar(64)                   null,
    solve_commit_date datetime                      null,
    uuid              varchar(36)                   null
    );

create index issue__uuid
    on issue (uuid);

create index issue_producer_index
    on issue (producer);

create index issue_repo_uuid_index
    on issue (repo_uuid);

create index issue_solver_index
    on issue (solver);

create index issue_type_index
    on issue (type);

create table if not exists issue_repo
(
    id                   int auto_increment
    primary key,
    repo_uuid            varchar(36) not null,
    branch               varchar(64) not null,
    tool                 varchar(16) not null,
    status               varchar(16) not null,
    scanned_commit_count int         not null,
    scan_time            mediumtext  null,
    total_commit_count   int         not null,
    start_commit         varchar(64) not null,
    start_scan_time      datetime    not null,
    end_scan_time        datetime    null
    );

create index issue_repo_repo_uuid_index
    on issue_repo (repo_uuid);

create table if not exists issue_repo_scan_list
(
    repo_uuid    varchar(36) not null
    primary key,
    branch       varchar(64) not null,
    status       varchar(16) not null,
    start_commit varchar(64) not null
    );

create table if not exists issue_scan
(
    uuid           varchar(36)  not null comment '主键'
    primary key,
    tool           varchar(45)  null comment '扫描工具类型',
    start_time     datetime     null comment '扫描开始时间',
    end_time       datetime     null comment '扫描结束时间',
    status         varchar(32)  not null comment '扫描完成状态',
    result_summary mediumtext   null comment '扫描结果内容总结',
    repo_uuid      varchar(36)  null comment '项目仓库id',
    commit_id      varchar(64)  null comment '本次commit id',
    commit_time    datetime     null comment '本次commit时间',
    author_time    datetime     null comment '本次 commit 的 author time',
    developer      varchar(64)  null comment '本次 commit 的开发者',
    parent_commit  varchar(255) null comment '本次 commit 的直接父节点',
    constraint uuid
    unique (uuid)
    );

create index idx_scan_repo_id
    on issue_scan (repo_uuid);

create index idx_scan_tool
    on issue_scan (tool);

create index issue_scan_commit_id_index
    on issue_scan (commit_id);

create table if not exists issue_type
(
    uuid                 varchar(36)                 not null comment '主键'
    primary key,
    type                 varchar(255)                null comment 'issue的具体类型',
    specification_source varchar(36)                 null comment '规则来源',
    category             varchar(128)                null comment 'issue所属的类别',
    description          mediumtext                  null comment 'issue的描述',
    language             varchar(45)                 null,
    status               varchar(32) default 'READY' null comment 'issue 状态，启用/弃用/测试等',
    severity             varchar(45)                 null,
    scope                varchar(32) default 'LINE'  not null comment '类级别、方法基本、代码块/行级别',
    constraint uuid
    unique (uuid)
    );

create index issue_type_type_index
    on issue_type (type);

create table if not exists location
(
    id            int auto_increment
    primary key,
    uuid          varchar(36)   not null comment '主键',
    start_line    mediumint     null comment 'bug所在上下文的开始行',
    end_line      mediumint     null comment 'bug所在上下文的结尾行',
    bug_lines     varchar(4096) null comment '表示这个bug在文件中具体体现在哪些行',
    start_token   mediumint     null,
    end_token     mediumint     null,
    file_name     varchar(512)  not null comment 'bug所在文件路径',
    class_name    varchar(256)  null comment 'bug所在类名',
    method_name   text          null comment 'bug所在方法名',
    rawIssue_uuid varchar(36)   not null comment 'bug所属rawissueID',
    code          text          null comment 'bug源代码',
    offset        int default 0 not null,
    repo_uuid     varchar(36)   null
    );

create index location_raw_issue_uuid_index
    on location (rawIssue_uuid);

create index location_repo_uuid_index
    on location (repo_uuid);

create table if not exists raw_issue
(
    id             int auto_increment
    primary key,
    uuid           varchar(36)  not null comment '主键',
    type           varchar(200) not null comment '缺陷类型',
    tool           varchar(45)  null comment 'rawissue类别',
    detail         mediumtext   null,
    file_name      varchar(512) null comment 'rawissue文件名',
    scan_uuid      varchar(36)  not null comment 'rawissue扫描id',
    issue_uuid     varchar(36)  null,
    commit_id      varchar(64)  not null comment '本次commit id',
    repo_uuid      varchar(36)  not null comment 'rawissue所属仓库id',
    code_lines     int          null,
    developer      varchar(64)  null,
    version        int          not null,
    raw_issue_hash varchar(36)  null
    );

create index idx_category_repoId
    on raw_issue (tool, repo_uuid);

create index idx_rawIssue_category
    on raw_issue (tool);

create index idx_rawIssue_issue_id
    on raw_issue (issue_uuid);

create index idx_rawIssue_repo_id
    on raw_issue (repo_uuid);

create index idx_rawIssue_repo_id_category
    on raw_issue (repo_uuid, tool);

create index idx_repo_id_hash
    on raw_issue (repo_uuid, raw_issue_hash);

create index idx_uuid_commit_status
    on raw_issue (uuid, tool, repo_uuid);

create table if not exists raw_issue_cache
(
    id             int auto_increment
    primary key,
    repo_uuid      char(36)                        not null,
    commit_id      varchar(64)                     not null,
    analyze_result longtext                        null,
    invoke_result  tinyint     default 1           null,
    tool           varchar(32) default 'sonarqube' not null,
    raw_issue_num  int                             null
    )
    engine = MyISAM;

create index idx_repo_uuid_commit_id
    on raw_issue_cache (repo_uuid, commit_id);

create table if not exists raw_issue_match_info
(
    id                int auto_increment
    primary key,
    cur_rawIssue_uuid varchar(36)  null,
    cur_commit_id     varchar(64)  null,
    pre_rawIssue_uuid varchar(36)  null,
    pre_commit_id     varchar(64)  null,
    issue_uuid        varchar(36)  null,
    status            varchar(64)  null,
    repo_uuid         varchar(36)  null,
    solve_way         varchar(512) null
    );

create index raw_issue_match_info_cur_commit_id_index
    on raw_issue_match_info (cur_commit_id);

create index raw_issue_match_info_issue_uuid_index
    on raw_issue_match_info (issue_uuid);

create index raw_issue_match_info_repo_uuid_index
    on raw_issue_match_info (repo_uuid);

create table if not exists scan_result
(
    id               int auto_increment comment '主键'
    primary key,
    category         varchar(45)                            not null comment '扫描结果类型',
    repo_uuid        varchar(36)                            not null comment '扫描的repo文件id',
    scan_date        date                                   not null comment '扫描日期',
    commit_id        varchar(64)                            not null comment '本次commit id',
    commit_date      datetime                               not null comment '本次commit时间',
    developer        varchar(64) collate utf8mb4_unicode_ci null comment '本次commit的提交者',
    new_count        int         default 0                  null comment '新增缺陷总数',
    eliminated_count int         default 0                  null comment '消除缺陷总数',
    remaining_count  int         default 0                  null comment '剩余缺陷总数 ',
    parent_commit_id varchar(64) default 'empty'            null,
    reopen_count     int         default 0                  null
    );

create index scan_result_repo_uuid_index
    on scan_result (repo_uuid);

create table if not exists violation
(
    id                     int auto_increment
    primary key,
    repo                   varchar(32)   not null,
    pr                     varchar(16)   not null,
    commit_id              char(40)      not null,
    code_source            varchar(16)   not null,
    file_path              varchar(2048) null,
    type                   varchar(512)  null,
    line                   varchar(4096) null,
    match_status           varchar(32)   null,
    detail                 varchar(4096) null,
    violation_uuid         char(36)      null,
    matched_violation_uuid char(36)      null,
    solve_way              varchar(64)   null,
    category               varchar(128)  null,
    owner                  varchar(64)   null
    );

create index idx_type
    on violation (type(26));

create index index_for_matching
    on violation (repo, pr, commit_id, code_source);

create index violation_pr_id_index
    on violation (id);

create index violation_pr_repo_index
    on violation (repo);

create index violation_pr_violation_uuid_index
    on violation (violation_uuid);

-- com2vio related table
create table if not exists repo_info
(   uuid             varchar(36)  not null
        primary key,
    repo             varchar(128) not null,
    owner            varchar(128) not null,
    base             varchar(64)  not null,
    api              varchar(256) null,
    token            varchar(128) null,
    commits          tinyint(1)   null,
    comments         tinyint(1)   null,
    files            tinyint(1)   null,
    pull_requests    tinyint(1)   null,
    commit_comments  tinyint(1)   null,
    files_downloaded tinyint(1)   null,
    matched          tinyint(1)   null,
    labelled         tinyint(1)   null,
    post_processed   tinyint(1)   null,
    constraint repo_status_pk_2
        unique (repo, owner),
    constraint repo_status_uuid_uindex
        unique (uuid)
    );

create table if not exists commit_comment
(
    start_line   int          null,
    end_line     int          null,
    content      text         null,
    id           int auto_increment
    primary key,
    commit_id    char(40)     null,
    owner        varchar(64)  not null,
    repo         varchar(64)  not null,
    pull         int unsigned not null,
    comment_uuid char(40)     null,
    file         text         not null,
    constraint commit_comment_id_uindex
    unique (id),
    constraint commit_comment_pk
    unique (comment_uuid, owner, pull, repo, commit_id)
    );

create table if not exists pull_request
(
    number           int                                not null,
    state            varchar(16)                        null,
    locked           tinyint(1)                         null,
    title            text                               null,
    body             longtext                           null,
    created_at       datetime                           null,
    updated_at       datetime                           null,
    closed_at        datetime                           null,
    merged_at        datetime                           null,
    merge_commit_sha char(40)                           null,
    draft            tinyint(1)                         null,
    head             mediumtext                         null comment 'json string',
    base             mediumtext                         null comment 'json string',
    repo             varchar(64)                        not null,
    owner            varchar(64)                        not null,
    user             mediumtext                         null comment 'json string',
    node_id          varchar(128)                       not null,
    uuid             char(36)                           not null
    primary key,
    id               int unsigned                       not null comment 'github api id',
    file_count       int      default 0                 null,
    comment_count    int      default -1                null,
    commit_count     int      default 0                 null,
    meta_created_at  datetime default CURRENT_TIMESTAMP null comment 'data created timestamp',
    final_commit     char(40)                           null,
    constraint pull_request_pk_2
    unique (repo, owner, number)
    );

create table if not exists pull_request_comment
(
    uuid                   char(36)     not null
    primary key,
    id                     int unsigned not null,
    node_id                varchar(128) null,
    diff_hunk              longtext     null,
    path                   text         null,
    commit_id              char(40)     null,
    original_commit_id     char(40)     null,
    user                   text         null comment 'json string',
    body                   mediumtext   null,
    created_at             datetime     null,
    updated_at             datetime     null,
    start_line             int unsigned null,
    original_start_line    int unsigned null,
    start_side             varchar(5)   null,
    line                   int unsigned null,
    original_line          int unsigned null,
    side                   varchar(5)   null,
    owner                  varchar(64)  not null,
    repo                   varchar(64)  not null,
    pull                   int          not null,
    pull_request_review_id int          null,
    constraint pull_request_comment_pk
    unique (owner, repo, pull, id)
    );

create index pull_request_comment_owner_repo_pull_index
    on pull_request_comment (owner, repo, pull);

create table if not exists pull_request_commit
(
    uuid      char(36)     not null
    primary key,
    sha       char(40)     not null,
    node_id   varchar(128) not null,
    url       varchar(512) not null,
    commit    text         null comment 'json string',
    author    text         null comment 'json string',
    committer text         null comment 'json string',
    parents   text         null comment 'json string, used for downloading files',
    owner     varchar(64)  not null,
    repo      varchar(64)  null,
    pull      int          not null,
    idx       int unsigned not null comment 'index of a commit in a pull request',
    constraint pull_request_commit_pk
    unique (owner, repo, pull, sha)
    )
    comment '2023/3';

create index pull_request_commit_owner_repo_pull_index
    on pull_request_commit (owner, repo, pull);

create index pull_request_commit_sha_index
    on pull_request_commit (sha);

create table if not exists pull_request_commit_file_download
(
    filename   varchar(1024) not null,
    version    varchar(9)    not null,
    url        varchar(1024) not null,
    downloaded tinyint       null comment '0 - not start, 1 - success, 2 - failed',
    id         int unsigned auto_increment
    primary key,
    commit     char(40)      not null,
    pull       int           not null,
    owner      varchar(64)   not null,
    repo       varchar(64)   not null
    )
    charset = utf8mb3;

create table if not exists pull_request_file
(
    previous_filename varchar(512)  null,
    uuid              varchar(36)   not null
    primary key,
    sha               char(40)      not null,
    filename          varchar(512)  not null,
    status            varchar(16)   not null,
    additions         int unsigned  null,
    deletions         int unsigned  null,
    changes           int unsigned  null,
    blob_url          varchar(1024) not null,
    raw_url           varchar(1024) not null,
    contents_url      varchar(1024) not null,
    owner             varchar(64)   not null,
    repo              varchar(64)   not null,
    pull              int           not null,
    constraint pull_request_file_pk
    unique (owner, repo, pull, sha, filename)
    )
    comment '2023/3';

create table if not exists used_pull_request
(
    owner  varchar(64) null,
    id     int auto_increment
    primary key,
    repo   varchar(64) null,
    number int         null,
    constraint used_pull_request_pk_2
    unique (owner, repo, number)
    );

create table if not exists violation_comment_match
(
    id             int auto_increment
    primary key,
    owner          varchar(64)   null,
    repo           varchar(64)   null,
    pull           int           null,
    commit_id      char(40)      null,
    file_path      varchar(2048) null,
    violation_uuid char(40)      not null,
    type           varchar(512)  null,
    detail         varchar(4096) null,
    line           varchar(4096) null,
    comment_uuid   char(40)      null,
    start_line     int           null,
    end_line       int           null,
    comment        text          null,
    vio_code       text          null,
    cmt_code       text          null,
    offset         int           null,
    label          tinyint       null,
    constraint violation_comment_match_pk
    unique (repo, owner, violation_uuid, comment_uuid)
    );

create table if not exists violation_tr_per_project
(
    tr          decimal(24, 4) null,
    occurrences int            null,
    repo        varchar(64)    null,
    type        varchar(512)   null,
    te          bigint         null,
    tc          bigint         null,
    severity    varchar(45)    null,
    category    varchar(128)   null
    );

create table if not exists rules_for_caring_or_not
(
    solve_way              varchar(64)  null,
    code_source            varchar(16)  not null,
    id                     int auto_increment
        primary key,
    matched_violation_uuid char(36)     null,
    violation_uuid         char(36)     not null,
    concern                tinyint(1)   null,
    category               varchar(128) not null,
    match_status           varchar(16)  not null,
    type                   varchar(512) not null,
    repo                   varchar(32)  not null,
    owner                  varchar(64)  not null,
    person_type            varchar(16)  null
);

create index idx_violation_uuid
    on rules_for_caring_or_not (violation_uuid);

create or replace definer = root@`%` view violation_on_current as
select min(`violation`.`id`)        AS `id`,
       `violation`.`owner`          AS `owner`,
       `violation`.`repo`           AS `repo`,
       `violation`.`pr`             AS `pr`,
       `violation`.`commit_id`      AS `commit_id`,
       `violation`.`code_source`    AS `code_source`,
       `violation`.`file_path`      AS `file_path`,
       `violation`.`line`           AS `line`,
       `violation`.`type`           AS `type`,
       `violation`.`detail`         AS `detail`,
       `violation`.`violation_uuid` AS `violation_uuid`
from `violation`
where (`violation`.`code_source` = 'current')
group by `violation`.`owner`, `violation`.`repo`, `violation`.`pr`,
         `violation`.`commit_id`, `violation`.`code_source`,
         `violation`.`file_path`, `violation`.`line`, `violation`.`type`,
         `violation`.`detail`, `violation`.`violation_uuid`;

