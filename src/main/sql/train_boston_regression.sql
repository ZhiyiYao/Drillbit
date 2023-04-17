set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`boston_general_regression`;

create table dfs.tmp.`boston_general_regression` (model)
as select
train_general_regression(
    feature,
    target,
    '-dense -iters 50 -eta0 0.1 -regularization l2 -opt adam'
)
as model
from dfs.tmp.`boston_train`;

drop table dfs.tmp.`boston_general_regression_predicted`;

create table dfs.tmp.`boston_general_regression_predicted` (feature, target, predicted)
as select
dataset.feature as feature,
dataset.target as target,
practice_general_regression(
    dataset.feature,
    model.model
) as predicted
from dfs.tmp.`boston_test` dataset
cross join dfs.tmp.`boston_general_regression` model;

select mae(
    target,
    predicted
)
from dfs.tmp.`boston_general_regression_predicted`;

select mse(
    target,
    predicted
)
from dfs.tmp.`boston_general_regression_predicted`;
