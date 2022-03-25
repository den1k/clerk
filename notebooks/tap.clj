^{:nextjournal.clerk/visibility :hide}
(ns ^:nextjournal.clerk/no-cache nextjournal.clerk.tap
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            [clojure.core :as core]))

^{::clerk/viewer {:transform-fn (fn [{::clerk/keys [var-from-def]}]
                                  {:var-name (symbol var-from-def) :value @@var-from-def})
                  :commands []
                  :fetch-fn (fn [_ x] x)
                  :render-fn '(fn [{:keys [var-name value]}]
                                (v/html
                                 (let [choices [:stream :latest]]
                                   [:div.flex.justify-between.items-center
                                    (into [:div.flex.items-center.font-sans.text-xs.mb-3 [:span.text-slate-500.mr-2 "View-as:"]]
                                          (map (fn [choice]
                                                 [:button.px-3.py-1.font-medium.hover:bg-indigo-50.rounded-full.hover:text-indigo-600.transition
                                                  {:class (if (= value choice) "bg-indigo-100 text-indigo-600" "text-slate-500")
                                                   :on-click #(v/clerk-eval `(reset! ~var-name ~choice))}
                                                  choice]) choices))
                                    [:button.text-xs.rounded-full.px-3.py-1.border-2.font-sans.hover:bg-slate-100.cursor-pointer {:on-click #(v/clerk-eval `(reset! !taps ()))} "Clear"]])))}}
(defonce !view (atom :stream))


^{::clerk/viewer clerk/hide-result}
(defonce !taps (atom ()))

^{::clerk/viewer clerk/hide-result}
(def taps-viewer {:render-fn '(fn [taps opts]
                                (v/html [:div.flex.flex-col
                                         (map (fn [tap] (let [{:keys [value key]} (:nextjournal/value tap)]
                                                         (with-meta [v/inspect value] {:key key})))
                                              taps)]))
                  :transform-fn (fn [taps]
                                  (mapv (fn [tap] (clerk/with-viewer {:fetch-fn (fn [{:as opts :keys [describe-fn]} x]
                                                                                 (prn :opts (select-keys opts [:path]) :x x)
                                                                                 (update x :value describe-fn (assoc opts :!budget (atom 100)) (:path opts)))}
                                                   tap)) taps))})


^{::clerk/viewer (if (= :latest @!view)
                   {:transform-fn first}
                   taps-viewer)}
@!taps



^{::clerk/viewer taps-viewer}
[{:key (str (gensym)) :value (range 10)}
 {:key (str (gensym)) :value (range 15)}
 {:key (str (gensym)) :value (range 20)}
 {:key (str (gensym)) :value (range 25)}
 {:key (str (gensym)) :value (range 30)}]



#_(reset! !taps ())


^{::clerk/viewer clerk/hide-result}
(defn tapped [x]
  (swap! !taps conj {:value x :inst (java.time.Instant/now) :key (str (gensym))})
  (binding [*ns* (find-ns 'tap)]
    (clerk/recompute!)))

#_(tapped (rand-int 1000))

#_(reset! @(find-var 'clojure.core/tapset) #{})

^{::clerk/viewer clerk/hide-result}
(defonce setup
  (add-tap tapped))

#_(remove-tap tapped)



^{::clerk/viewer clerk/hide-result}
(comment
  (Thread/sleep 4000)
  
  (tap> (rand-int 1000))
  (tap> (shuffle (range 100)))
  (tap> (javax.imageio.ImageIO/read (java.net.URL. "https://images.freeimages.com/images/large-previews/773/koldalen-4-1384902.jpg")))
  (tap> (clerk/vl {:width 650 :height 400 :data {:url "https://vega.github.io/vega-datasets/data/us-10m.json"
                                                 :format {:type "topojson" :feature "counties"}}
                   :transform [{:lookup "id" :from {:data {:url "https://vega.github.io/vega-datasets/data/unemployment.tsv"}
                                                    :key "id" :fields ["rate"]}}]
                   :projection {:type "albersUsa"} :mark "geoshape" :encoding {:color {:field "rate" :type "quantitative"}}}))

  )
