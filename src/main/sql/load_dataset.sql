drop table dfs.tmp.`placeholder`;

create table dfs.tmp.`placeholder` (placeholder) as select
full_name
from cp.`employee.json`;

drop table dfs.tmp.`iris`;

create table dfs.tmp.`iris` (feature, target, train_or_test) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target,
split_train_test_sample(
    feature_and_target,
    0.7
) as train_or_test
from (
    select
    load_iris_dataset(placeholder, '-n_samples 150') as feature_and_target
    from dfs.tmp.`placeholder`
    limit 150
) t;

drop table dfs.tmp.`iris_train`;

create table dfs.tmp.`iris_train` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`iris`
where train_or_test = 'train';

drop table dfs.tmp.`iris_test`;

create table dfs.tmp.`iris_test` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`iris`
where train_or_test = 'test';

drop table dfs.tmp.`digits`;

create table dfs.tmp.`digits` (feature, target, train_or_test) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target,
split_train_test_sample(
    feature_and_target,
    0.7
) as train_or_test
from (
    select
    load_digits_dataset(placeholder, '-n_samples 1000') as feature_and_target
    from dfs.tmp.`placeholder`
    limit 1000
) t;

drop table dfs.tmp.`digits_train`;

create table dfs.tmp.`digits_train` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`digits`
where train_or_test = 'train';

drop table dfs.tmp.`digits_test`;

create table dfs.tmp.`digits_test` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`digits`
where train_or_test = 'test';

drop table dfs.tmp.`boston`;

create table dfs.tmp.`boston` (feature, target, train_or_test) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target,
split_train_test_sample(
    feature_and_target,
    0.7
) as train_or_test
from (
    select
    load_boston_dataset(placeholder, '-n_samples 506') as feature_and_target
    from dfs.tmp.`placeholder`
    limit 506
) t;

drop table dfs.tmp.`boston_train`;

create table dfs.tmp.`boston_train` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`boston`
where train_or_test = 'train';

drop table dfs.tmp.`boston_test`;

create table dfs.tmp.`boston_test` (feature, target) as select
feature as feature,
target as target
from dfs.tmp.`boston`
where train_or_test = 'test';