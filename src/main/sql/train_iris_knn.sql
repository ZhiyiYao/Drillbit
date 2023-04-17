set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`iris_knn`;

create table dfs.tmp.`iris_knn` (model)
as select
train_k_neighbors_classification(
    feature,
    target,
    '-dense -dims 5'
)
as model
from dfs.tmp.`iris_train`;

drop table dfs.tmp.`iris_knn_predicted`;

create table dfs.tmp.`iris_knn_predicted` (feature, target, brute_predicted, brute_predicted_1)
as select
dataset.feature as feature,
dataset.target as target,
practice_k_neighbors_classification(
    dataset.feature,
    model.model,
    '-solver brute -k 5 -weight uniform'
) as brute_predicted,
practice_k_neighbors_classification(
    dataset.feature,
    model.model,
    '-solver brute -k 3 -weight inverse'
) as brute_predicted_1
from dfs.tmp.`iris_test` dataset
cross join dfs.tmp.`iris_knn` model;

select target, brute_predicted, brute_predicted_1 from
dfs.tmp.`iris_knn_predicted`
limit 5;

select acc(
    target,
    brute_predicted
)
from dfs.tmp.`iris_knn_predicted`;

select acc(
    target,
    brute_predicted_1
)
from dfs.tmp.`iris_knn_predicted`;

select cm(
    target,
    brute_predicted
)
from dfs.tmp.`iris_knn_predicted`;

select cm(
    target,
    brute_predicted_1
)
from dfs.tmp.`iris_knn_predicted`;
