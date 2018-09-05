package com.sidbola.cryptrack.features.coindetails.news

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.features.shared.model.Article
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_news.view.*

class NewsListAdapter(
    passedArticleList: MutableList<Article>,
    var context: Context
)
    : RecyclerView.Adapter<NewsListAdapter.ArticleViewHolder>() {

    private var articleList: MutableList<Article> = passedArticleList

    val itemClickStream: PublishSubject<View> = PublishSubject.create()

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentArticle = articleList[position]

        setFadeAnimation(holder.view.articleName!!)
        setFadeAnimation(holder.view.articleDescription)
        setFadeAnimation(holder.view.articleAuthor)
        setFadeAnimation(holder.view.articleDate)

        val year: String
        val month: String
        val day: String

        var data = currentArticle.publishedAt.split("T")
        val date = data[0]
        data = date.split("-")
        year = data[0]
        day = data[2]

        month = when (data[1]) {
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> data[1]
        }

        val formattedDate = "$month $day, $year"

        holder.view.articleName?.text = currentArticle.title
        holder.view.articleAuthor.text = currentArticle.author
        holder.view.articleDate.text = formattedDate
        holder.view.articleDescription.text = currentArticle.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val coinItem = layoutInflater.inflate(R.layout.item_news, parent, false)
        return ArticleViewHolder(coinItem)
    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        view.startAnimation(anim)
    }

    inner class ArticleViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener { v -> itemClickStream.onNext(v) }
        }
    }
}
