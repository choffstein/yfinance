(ns #^{:author "Baishampayan Ghose"
       :doc "A few simple utils to download Y! Finance data"}
  in.freegeek.yfinance
  (:require [clj-time.core :as time]
            [clj-http.client :as client])
  (:import java.net.URLEncoder))

(def #^{:private true} +base-url+ "http://itable.finance.yahoo.com/table.csv?s=%s&g=d&a=%d&b=%d&c=%d&d=%d&e=%d&f=%d")

(defn- get-full-url
  "Construct the complete URL given the params"
  [y1 m1 d1 y2 m2 d2 sym]
  (let [start (time/date-time y1 m1 d1)
        end (time/date-time y2 m2 d2)]
    (format +base-url+
            (URLEncoder/encode sym "UTF-8")
            (dec (time/month start))
            (time/day start)
            (time/year start)
            (dec (time/month end))
            (time/day end)
            (time/year end))))

(defn- fetch-url
  "Fetch one URL using HTTP Agent"
  [url]
  (client/get url {:throw-exceptions false}))


(defn- collect-response
  "Wait for all the agents to finish and then return the response"
  [& responses]
  (for [response responses]
    (if (= (:status response) 200)
      (:body response)
      (:status response))))


(defn fetch-historical-data
  "Fetch historical prices from Yahoo! finance for the given symbols between start and end"
  [start end syms]
  (letfn [(parse-date [^String dt] (map #(Integer/parseInt %) (.split dt "-")))]
    (let [[y1 m1 d1] (parse-date start)
          [y2 m2 d2] (parse-date end)
          urls (map (partial get-full-url y1 m1 d1 y2 m2 d2) syms)
          responses (map fetch-url urls)]
      (zipmap syms (apply collect-response responses)))))
