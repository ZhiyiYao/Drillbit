set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`digits_knn`;

create table dfs.tmp.`digits_knn` (model)
as select
train_k_neighbors_classification(
    feature,
    target,
    '-dense -dims 5'
)
as model
from dfs.tmp.`digits_train`;

drop table dfs.tmp.`digits_knn_predicted`;

create table dfs.tmp.`digits_knn_predicted` (feature, target, brute_predicted, kdtree_predicted)
as select
dataset.feature as feature,
dataset.target as target,
practice_k_neighbors_classification(
    dataset.feature,
    model.model,
    '-solver brute -k 10'
) as brute_predicted,
practice_k_neighbors_classification(
    dataset.feature,
    model.model,
    '-solver kdtree -k 10'
) as kdtree_predicted
from dfs.tmp.`digits_test` dataset
cross join dfs.tmp.`digits_knn` model;

select acc(
    target,
    brute_predicted
)
from dfs.tmp.`digits_knn_predicted`;

select acc(
    target,
    kdtree_predicted
)
from dfs.tmp.`digits_knn_predicted`;