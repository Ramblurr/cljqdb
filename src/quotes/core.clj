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

(defn wrap-charset [handler charset]
  (fn [request]
    (if-let [response (handler request)]
      (if-let [content-type (get-in response [:headers "Content-Type"])]
        (if (.contains content-type "charset")
          response
          (assoc-in response
            [:headers "Content-Type"]
            (str content-type "; charset=" charset)))
        response))))

(defn wrap-remote-addr [app]
  (fn [request]
    (let [request (assoc request :real-remote-addr (if (contains? (request :headers) "x-real-ip")
      ((request :headers) "x-real-ip")
      (request :remote-addr)))]
    (app request))))

(defn render [t]
  {:status 200, :headers {"Content-Type" "text/html; charset=utf-8"}, :body t})
(defroutes main-routes
  (GET "/" [] (render (controller/index)))
  ;(GET "/debug" req (render (str req)))
  (GET "/quotes/submit" [] (render (controller/quote-form)))
  (GET "/quotes/:id" [id] (render (controller/quote-view id)))
  (GET "/quotes/:id/votes" [id] (controller/votes id))
  (PUT "/quotes/:id/votes" {remote-addr :real-remote-addr params :params} (controller/put-votes remote-addr params))
  (GET "/quotes" {params :params} (render (controller/browse-quotes params)))
  (GET "/top" {params :params} (render (controller/top-quotes params)))
  (GET "/random" {params :params} (render (controller/random-quotes params)))
  (POST "/quotes" {params :params} (controller/quote-form-submit params))
;  (PUT "/quotes" {params :params} (handle-submit params))
  (route/resources "/")
)

(def app
    (-> main-routes
        compojure.handler/site
        wrap-remote-addr
        (wrap-charset (:charset "utf8"))
      ))

(defonce server (run-jetty #'app
                  {:join? false
                  :port 8080}))
