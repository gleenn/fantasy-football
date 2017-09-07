(ns sportball.dev-middleware
  (:require [ring.middleware.reload :refer [wrap-reload]]
            [selmer.middleware :refer [wrap-error-page]]
            [prone.middleware :refer [wrap-exceptions]]))

(defn wrap-dev [handler]
  (-> handler
      wrap-reload
      wrap-error-page
      (wrap-exceptions {;:app-namespaces     ['your-ns-1 'my.ns.to-show]
                        ;:skip-prone?        (fn [req] (not-browser? req))
                        :print-stacktraces? false})))
