(ns quotes.views
 (:require [clj-time.core :as time]
           [clj-time.format :as format]
           [clj-time.coerce :as coerce]
           [net.cgrand.enlive-html :as html]
           [clj-json [core :as json]]))

(defn- emit-json
  "Turn the object to JSON, and emit it with the correct content type.
    Source: http://wiki.sproutcore.com/w/page/27098640/Todos%2006-Building%20with%20Compojure%20and%20MongoDB "
  [x]
  {:headers {"Content-Type" "application/json"}
   :body    (json/generate-string x)})

(def *context*
  {:header {:title "VT Bash" :href "/"
   :nav [{:text "Top" :href "/top"}
         {:text "Browse" :href "/quotes"}
         {:text "Random" :href "/random"}
         {:text "Submit" :href "/quotes/submit"}]}})


(def *nav-sel* [[:.nav-item]])

(html/defsnippet nav-model "views/index.html" *nav-sel*
  [{:keys [text href]}]
  [:a] (html/do->
        (html/content text)
        (html/set-attr :href href)))

(html/defsnippet header-model "views/index.html" [[:div#header]]
  [{:keys [title href nav]}]
  [:h1#title] (html/content {:tag :a, :attrs {:href href}, :content title})
  [:ul#navigation] (html/content (map nav-model nav)))

(html/defsnippet welcome-model "views/welcome.html" [[:#welcome-box]] [])

(html/defsnippet quote-form-model "views/quote-form.html" [[:#submit-form]] [])

(html/defsnippet quotes-browse-model "views/quotes-browse.html" [[:#quotes-content]] [quotes prev next]
  [:a.next-page] #(when (not (nil? next)) ((html/do-> (html/html-content "Next Page →") (html/set-attr :href (format "?start=%s" next))) %))
  [:a.prev-page] #(when (not (nil? prev)) ((html/set-attr :href (format "?start=%s" prev)) %))
  [:li.quote] (html/clone-for
    [{:keys [body timestamp id flagged tags up down]} quotes]
    [:h3] (html/set-attr :id (str "quote-header-" id))
    [:blockquote.quote-body :pre] (html/content body)
    [:span.quote-id] (html/content (str "#" id))
    [:a.quote-permalink] (html/set-attr :href (str "/quotes/" id))
    [:a.quote-rating-up] (html/do-> (html/remove-class "casted-vote") (html/set-attr :href (format "/quotes/%s/votes" id) :id (str "quote-rating-up-" id)))
    [:a.quote-rating-down] (html/do-> (html/remove-class "casted-vote")(html/set-attr :href (format "/quotes/%s/votes" id) :id (str "quote-rating-down-" id)))
    [:span#quote-live-vote-result-ID] (html/do-> (html/set-attr :id (str "quote-live-vote-result-" id)) (html/content ""))
    [:span.quote-rating] (html/do-> (html/set-attr :id (str "quote-rating-" id)) (html/content (str (- up down))))
    [:span#quote-vote-count-ID] (html/do-> (html/set-attr :id (str "quote-vote-count-" id)) (html/content (str (+ up down))))
    [:span.quote-flagged] #(when (true? flagged) %)
    [:a.quote-report] #(when (not flagged) ((html/set-attr :id (str "quote-report-" id)) %))
        ;((html/content "[REPORT]"))); (html/set-attr :id (str "quote-report-" id) :href "#"))))
    [:span.quote-date] (html/content (format/unparse (format/formatters :rfc822) (coerce/from-long timestamp)))
    [:div.quote-tags :a] (html/clone-for [tag tags] (html/do-> (html/set-attr :href (str "/tags/" tag) :title (str "View quotes tagged " tag)) (html/content (str tag " "))))
    ))

(html/defsnippet simple-message-model (html/html-snippet "<div id=\"message\"><h2></h2><p></p></div>") [:#message]
  [{:keys [title text]}]
  [:h2] (html/content title)
  [:p]  (html/content text))

(defn index-content [content]
  [:#welcome-box] (html/content content))

(defn quote-form-content [content]
 [:#submit-form] (html/content content))

(defn simple-message-content [content]
  (html/content (simple-message-model content)))

(defn quotes-browse-content [content]
  [:#quotes-content] (html/content content))

(html/deftemplate base "views/index.html" [{:keys [header content]} content-model]
  [:#header] (html/substitute (header-model header))
  [:#content] (content-model content)
)

(defn index-html
  ([] (base (assoc *context* :content (welcome-model)) index-content)))

(defn quote-form-html
  ([] (base (assoc *context* :content (quote-form-model)) quote-form-content)))

(defn quote-submitted
  [success]
  (if (reduce #(true? (and %1 %2)) success) ; success is a list of bools, verify they are all true
    (base (assoc *context* :content {:title "Quote Submitted" :text "Thank you for submitting a quote to our database. A site administrator will review it shortly. If it gets approved, it will appear on this web site. Fingers crossed!"}) simple-message-content)
    (base (assoc *context* :content { :title "Submission Failed" :text "There was an error. Sorry :("}) simple-message-content)))

(defn browse-quotes-html [quotes prev next]
  (base (assoc *context* :content (quotes-browse-model quotes prev next)) quotes-browse-content))

(defn quote-view-html [quote]
  (base (assoc *context* :content (quotes-browse-model (list quote) nil nil)) quotes-browse-content))

(defn quote-votes [{:keys [u d]}]
  (emit-json {:up u :down d}))

(defn vote-result [success]
  (emit-json success))
