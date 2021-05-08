drop table dfs.tmp.`placeholder_for_loading_dataset`;

create table dfs.tmp.`placeholder_for_loading_dataset` (placeholder) as select
full_name
from cp.`employee.json`
limit 500;

drop table dfs.tmp.`iris`;

create table dfs.tmp.`iris` (feature, target) as select
add_bias(add_index(extract_feature(feature_and_target))) as feature,
extract_target(feature_and_target) as target
from (
    select
    load_iris_dataset(placeholder) as feature_and_target
    from dfs.tmp.`placeholder_for_loading_dataset`
    limit 150
) t;

create table dfs.tmp.`employee` (feature, target) as select
numerical_feature_values(
    concat_feature_names('department_id', 'position_id'),
    department_id,
    position_id
) as feature,
cast(salary as char) as target
from cp.`employee.json`
limit 500;