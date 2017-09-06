(ns ^:figwheel-no-load sportball.app
  (:require [sportball.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
