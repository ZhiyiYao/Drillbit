select train_regression(
    feature,
    target,
    '-iters 200 -opt adam -regularization l2'
)
from dfs.tmp.`employee`;