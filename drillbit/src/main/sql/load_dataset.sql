drop table dfs.tmp.`placeholder_for_loading_dataset`;

create table dfs.tmp.`placeholder_for_loading_dataset` (placeholder) as select
full_name
from cp.`employee.json`
limit 500;

drop table dfs.tmp.`iris`;

create table dfs.tmp.`iris` (feature, target) as select
extract_feature(feature_and_target) as feature,
extract_target(feature_and_target) as target
from (
    select
    load_iris_dataset(placeholder) as feature_and_target
    from dfs.tmp.`placeholder_for_loading_dataset`
    limit 150
) t;