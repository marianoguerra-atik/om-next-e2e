(ns om-next-e2e.handlers)

(defn action [{:keys [body]}]
  {:status 200 :body {:action body}})

(defn query [{:keys [body]}]
  {:status 200 :body {:query body}})

(defn not-found [_]
  {:status 404 :body {:error "Not Found"}})

