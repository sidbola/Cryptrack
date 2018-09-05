package com.sidbola.cryptrack.features.coindetails.news

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.coindetails.CoinInfoPageFragment
import com.sidbola.cryptrack.features.mainscreen.MainNavigationActivity
import com.sidbola.cryptrack.features.shared.model.Article
import com.sidbola.cryptrack.network.newsapi.GetNewsDataProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.item_news.view.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [NewsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class NewsFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var coinTicker: String = ""
    private var coinName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parent = parentFragment as CoinInfoPageFragment
        coinTicker = parent.getCoinTickerFromParent()
        coinName = parent.getCoinNameFromParent()

        if (!coinName.contains("/")) {

            val newsData = GetNewsDataProvider.provideGetCoinData()
            newsData.getNewsData(coinName, "crypto-coins-news", "a4ebfc82fb7144559fa409d9f9015b37")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .onErrorReturn { _ -> null }
                    .subscribe {

                        try {
                            val act = activity as MainNavigationActivity

                            val articleList: MutableList<Article> = it.articles

                            val listAdapter = NewsListAdapter(articleList, act)
                            listAdapter.itemClickStream.subscribe { v ->

                                val articleToDisplay: Article

                                for (article in articleList) {
                                    if (v.articleName.text == article.title) {
                                        articleToDisplay = article
                                        mListener?.onNewsFragmentInteraction(articleToDisplay)
                                        break
                                    }
                                }
                            }

                            if (articleList.size == 0) {
                                noNewsLabel.visibility = View.VISIBLE
                            }

                            newsRecyclerView.layoutManager = LinearLayoutManager(activity)
                            newsRecyclerView.adapter = listAdapter
                        } catch (e: Exception) {
                        }
                    }
        } else {
            noNewsLabel.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        fun onNewsFragmentInteraction(response: Article)
    }
} // Required empty public constructor
