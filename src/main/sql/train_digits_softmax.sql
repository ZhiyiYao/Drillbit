set `planner.enable_nljoin_for_scalar_only` = false;

drop table dfs.tmp.`digits_softmax`;

create table dfs.tmp.`digits_softmax` (model)
as select
train_softmax_regression(
    feature,
    target,
    '-dense -iters 50 -eta0 0.1'
)
as model
from dfs.tmp.`digits_train`;

drop table dfs.tmp.`digits_softmax_predicted`;

create table dfs.tmp.`digits_softmax_predicted` (feature, target, predicted, probability)
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
from dfs.tmp.`digits_test` dataset
cross join dfs.tmp.`digits_softmax` model;

select target, predicted
from dfs.tmp.`digits_softmax_predicted` limit 5;

select target, probability
from dfs.tmp.`digits_softmax_predicted` limit 5;

select acc(
    target,
    predicted
)
from dfs.tmp.`digits_softmax_predicted`;

select cm(
    target,
    predicted
)
from dfs.tmp.`digits_softmax_predicted`;