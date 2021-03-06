(ns sportball.middleware
  (:require [sportball.env :refer [defaults]]
            [cognitect.transit :as transit]
            [clojure.tools.logging :as log]
            [sportball.layout :refer [*app-context* error-page]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [muuntaja.core :as muuntaja]
            [muuntaja.format.transit :as transit-format]
            [muuntaja.middleware :refer [wrap-format wrap-params]]
            [sportball.config :refer [env]]

            [ring-ttl-session.core :refer [ttl-memory-store]] ; not used

            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends.session :refer [session-backend]]
            [ring.middleware.oauth2 :refer [wrap-oauth2]]
            [sportball.config :refer [env]]
            [clojure.tools.logging :as log])
  (:import [javax.servlet ServletContext]
           [org.joda.time ReadableInstant]))

(defn wrap-context [handler]
  (fn [request]
    (binding [*app-context*
              (if-let [context (:servlet-context request)]
                ;; If we're not inside a servlet environment
                ;; (for example when using mock requests), then
                ;; .getContextPath might not exist
                (try (.getContextPath ^ServletContext context)
                     (catch IllegalArgumentException _ context))
                ;; if the context is not specified in the request
                ;; we check if one has been specified in the environment
                ;; instead
                (:app-context env))]
      (handler request))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t)
        (error-page {:status 500
                     :title "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     (error-page
       {:status 403
        :title "Invalid anti-forgery token"})}))

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(def restful-format-options
  (update
    muuntaja/default-options
    :formats
    merge
    {"application/transit+json"
     {:decoder [(partial transit-format/make-transit-decoder :json)]
      :encoder [#(transit-format/make-transit-encoder
                   :json
                   (merge
                     %
                     {:handlers {org.joda.time.DateTime joda-time-writer}}))]}}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format restful-format-options))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn on-error [request response]
  (error-page
    {:status 403
     :title (str "Access to " (:uri request) " is not authorized")}))

(defn wrap-restricted [handler]
  (restrict handler {:handler authenticated?
                     :on-error on-error}))

(defn wrap-auth [handler]
  (let [backend (session-backend)]
    (-> handler
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defn wrap-base [handler]
  (-> handler
      ((:middleware defaults))
      wrap-auth
      (wrap-oauth2
        {:yahoo
         {:client-id        (env :client-id)
          :client-secret    (env :client-secret)
          :authorize-uri    "https://api.login.yahoo.com/oauth2/request_auth"
          :access-token-uri "https://api.login.yahoo.com/oauth2/get_token"
          :scopes           ["fspt-w"]
          :launch-uri       "/oauth2/yahoo"
          :redirect-uri     "/oauth2/yahoo/callback"
          :landing-uri      "/fantasy"}})
      wrap-webjars
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in [:session :store] (ttl-memory-store (* 60 30)))
            #_(assoc-in [:session :store] (cookie-store {:key (doto log/info (env :cookie-secret-key))}))
            #_(assoc-in [:session :cookie-name] "example-app-sessions")
            (assoc-in [:session :cookie-attrs :same-site] :lax)))
      wrap-context
      #_wrap-internal-error))
