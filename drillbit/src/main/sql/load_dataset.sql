drop table dfs.tmp.`placeholder`;

create table dfs.tmp.`placeholder` (placeholder) as select
full_name
from cp.`employee.json`;

drop table dfs.tmp.`iris`;

select dataset_description('iris');

create table dfs.tmp.`iris` (feature, target) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target
from (
    select
    load_iris_dataset(placeholder, '-n_samples 150') as feature_and_target
    from dfs.tmp.`placeholder`
    limit 150
) t;

drop table dfs.tmp.`digits`;

select dataset_description('digits');

create table dfs.tmp.`digits` (feature, target) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target
from (
    select
    load_digits_dataset(placeholder, '-n_samples 500') as feature_and_target
    from dfs.tmp.`placeholder`
    limit 500
) t;

drop table dfs.tmp.`boston`;

select dataset_description('boston');

create table dfs.tmp.`boston` (feature, target) as select
add_bias(
    add_index(
        extract_feature(
            feature_and_target
        )
    )
) as feature,
extract_target(
    feature_and_target
) as target
from (
    select
    load_boston_dataset(placeholder) as feature_and_target
    from dfs.tmp.`placeholder`
    limit 506
) t;