package com.sksamuel.elastic4s.search

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DiscoveryLocalNodeProvider
import org.scalatest.{FlatSpec, Matchers}

class TermsQueryTest
  extends FlatSpec
    with DiscoveryLocalNodeProvider
    with Matchers
    with ElasticDsl {

  http.execute {
    createIndex("lords").mappings(
      mapping("people").fields(
        keywordField("name")
      )
    )
  }.await

  http.execute {
    bulk(
      indexInto("lords/people") fields ("name" -> "nelson"),
      indexInto("lords/people") fields ("name" -> "edmure"),
      indexInto("lords/people") fields ("name" -> "umber"),
      indexInto("lords/people") fields ("name" -> "byron")
    ).refresh(RefreshPolicy.Immediate)
  }.await

  "a terms query" should "find multiple terms using 'or'" in {

    val resp = http.execute {
      search("lords") query termsQuery("name", "nelson", "byron")
    }.await.right.get.result

    resp.hits.hits.map(_.sourceAsString).toSet shouldBe Set("""{"name":"nelson"}""", """{"name":"byron"}""")
  }
}
