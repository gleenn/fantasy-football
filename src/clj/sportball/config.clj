(ns sportball.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [mount.core :refer [args defstate]]
            [clojure.string :as str]))

(defstate env :start (let [config (load-config :merge [(args)
                                                       (source/from-system-props)
                                                       (source/from-env)])]
                       (if (str/blank? (:cookie-secret-key config))
                         (throw (Exception. "Please set :cookie-secret-key"))
                         config)))
