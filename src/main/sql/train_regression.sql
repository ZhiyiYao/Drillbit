set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`regression_model`;

create table dfs.tmp.`regression_model` (model) as select
train_regression(
    feature,
    target,
    '-iters 200 -opt adam -regularization l2'
) as model
from dfs.tmp.`employee`;

set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`iris_model`;

create table dfs.tmp.`iris_model` (model)
as select
train_softmax_regression(
    feature,
    target,
    '-dense -iters 500 -eta0 0.1'
)
as model
from dfs.tmp.`iris_train`;

drop table dfs.tmp.`iris_model_predicted`;

create table dfs.tmp.`iris_model_predicted` (feature, target, predicted, probability)
as select
dataset.feature as feature,
dataset.target as target,
practice_softmax_regression(
    dataset.feature,
    model.model
) as predicted,
practice_softmax_regression(
    dataset.feature,
    model.model,
    '-return_proba'
) as probability
from dfs.tmp.`iris_test` dataset
cross join dfs.tmp.`iris_model` model;

select acc(
    target,
    predicted
)
from dfs.tmp.`iris_model_predicted`;
