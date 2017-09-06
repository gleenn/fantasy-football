(ns sportball.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sportball.core-test]))

(doo-tests 'sportball.core-test)

