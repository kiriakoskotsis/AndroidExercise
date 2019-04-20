package com.kiriakos_kotsis.androidexercise.main

import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kiriakos_kotsis.androidexercise.R
import com.kiriakos_kotsis.androidexercise.entities.Comment
import com.kiriakos_kotsis.androidexercise.entities.Post
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class DetailActivity : AppCompatActivity() {

    private var detailAdapter:DetailAdapter? = null
    private var comments:ArrayList<Comment> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView:RecyclerView
    private lateinit var currentPost:Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        currentPost = if(savedInstanceState != null) savedInstanceState.getSerializable(MasterAdapter.PostHolder.POST_KEY) as Post
                      else intent?.extras?.get(MasterAdapter.PostHolder.POST_KEY) as Post
        recyclerView = findViewById(R.id.detail_recycler_view)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager

        val task = CommentsAsyncTask(this)
        task.execute("GET")
    }

    fun updateUI() {
        detailAdapter = DetailAdapter(comments, currentPost)
        recyclerView.adapter = detailAdapter
    }

    class CommentsAsyncTask(private val activity:DetailActivity): AsyncTask<String, Unit, Unit>() {

        override fun doInBackground(vararg p0: String) {
            val url = URL(MasterActivity.request_url + "posts/" + activity.currentPost.id + "/comments")

            if (p0[0] == "POST") {
                val client = OkHttpClient()
                val body:RequestBody = RequestBody.create(MediaType.get("application/json; charset=utf-8"), p0[1])
                val request:Request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
                try {
                    val response:Response = client.newCall(request).execute()
                    if(response.isSuccessful)
                        Toast.makeText(activity, "Comment posted successfully.", Toast.LENGTH_LONG).show()
                    else
                        Toast.makeText(activity, "An error occurred while posting comment.", Toast.LENGTH_LONG).show()
                } catch (e:Exception) {
                        e.printStackTrace()
                    }
            }
            else {
                // Retrieve all comments
                val jsonResponse = JSONArray(url.readText())

                for (i in 0 until jsonResponse.length()) {
                    val postJson: JSONObject = jsonResponse.getJSONObject(i)

                    val id: Int = postJson.getInt("id")
                    val author: String = postJson.getString("author")
                    val email: String = postJson.getString("email")
                    val comment: String = postJson.getString("comment")

                    activity.comments.add(Comment(id, activity.currentPost.id, author, email, comment))
                }
            }
        }

        override fun onPostExecute(result: Unit?) {
            activity.updateUI()
        }
    }
}