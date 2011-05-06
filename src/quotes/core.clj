(ns quotes.core
  (:use compojure.core
        compojure.handler
        ring.adapter.jetty
        ring.middleware.reload
        ring.middleware.stacktrace
        ring.middleware.params
        ring.middleware.session)
  (:require [quotes.controller :as controller]
            [compojure.route :as route]))


(defroutes main-routes
  (GET "/" [] (controller/index))
  (GET "/quotes/:id" [id] (controller/quote-view id))
  (GET "/quotes/:id/votes" [id] (controller/votes id))
  (PUT "/quotes/:id/votes" {remote-addr :remote-addr params :params} (controller/put-votes remote-addr params))
  (GET "/quotes" [] (controller/browse-quotes))
  (GET "/quotes/submit" [] (controller/quote-form))
  (POST "/quotes" {params :params} (controller/quote-form-submit params))
;  (PUT "/quotes" {params :params} (handle-submit params))
  (route/resources "/")
)

(def app
    (compojure.handler/site main-routes))

(defonce server (run-jetty #'app
                  {:join? false
                  :port 8080}))
