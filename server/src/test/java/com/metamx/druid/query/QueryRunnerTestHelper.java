/*
 * Druid - a distributed column store.
 * Copyright (C) 2012  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.metamx.druid.query;

import com.google.common.collect.Lists;
import com.metamx.druid.Query;
import com.metamx.druid.QueryGranularity;
import com.metamx.druid.StorageAdapter;
import com.metamx.druid.aggregation.AggregatorFactory;
import com.metamx.druid.aggregation.CountAggregatorFactory;
import com.metamx.druid.aggregation.DoubleSumAggregatorFactory;
import com.metamx.druid.aggregation.LongSumAggregatorFactory;
import com.metamx.druid.aggregation.post.ArithmeticPostAggregator;
import com.metamx.druid.aggregation.post.ConstantPostAggregator;
import com.metamx.druid.aggregation.post.FieldAccessPostAggregator;
import com.metamx.druid.index.v1.IncrementalIndex;
import com.metamx.druid.index.v1.IncrementalIndexStorageAdapter;
import com.metamx.druid.index.v1.Index;
import com.metamx.druid.index.v1.IndexStorageAdapter;
import com.metamx.druid.index.v1.MMappedIndex;
import com.metamx.druid.index.v1.MMappedIndexStorageAdapter;
import com.metamx.druid.index.v1.TestIndex;
import com.metamx.druid.query.segment.MultipleIntervalSegmentSpec;
import com.metamx.druid.query.segment.QuerySegmentSpec;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 */
public class QueryRunnerTestHelper
{
  public static final String dataSource = "testing";
  public static final QueryGranularity gran = QueryGranularity.DAY;
  public static final QueryGranularity allGran = QueryGranularity.ALL;
  public static final String providerDimension = "provider";
  public static final String qualityDimension = "quality";
  public static final String placementishDimension = "placementish";
  public static final String indexMetric = "index";
  public static final CountAggregatorFactory rowsCount = new CountAggregatorFactory("rows");
  public static final LongSumAggregatorFactory indexLongSum = new LongSumAggregatorFactory("index", "index");
  public static final DoubleSumAggregatorFactory indexDoubleSum = new DoubleSumAggregatorFactory("index", "index");
  public static final ConstantPostAggregator constant = new ConstantPostAggregator("const", 1L);
  public static final FieldAccessPostAggregator rowsPostAgg = new FieldAccessPostAggregator("rows", "rows");
  public static final FieldAccessPostAggregator indexPostAgg = new FieldAccessPostAggregator("index", "index");
  public static final ArithmeticPostAggregator addRowsIndexConstant =
      new ArithmeticPostAggregator(
          "addRowsIndexConstant", "+", Lists.newArrayList(constant, rowsPostAgg, indexPostAgg)
      );
  public static final List<AggregatorFactory> commonAggregators = Arrays.asList(rowsCount, indexDoubleSum);


  public static final String[] expectedFullOnIndexValues = new String[]{
      "4500.0", "6077.949111938477", "4922.488838195801", "5726.140853881836", "4698.468170166016",
      "4651.030891418457", "4398.145851135254", "4596.068244934082", "4434.630561828613", "0.0",
      "6162.801361083984", "5590.292701721191", "4994.298484802246", "5179.679672241211", "6288.556800842285",
      "6025.663551330566", "5772.855537414551", "5346.517524719238", "5497.331253051758", "5909.684387207031",
      "5862.711364746094", "5958.373008728027", "5224.882194519043", "5456.789611816406", "5456.095397949219",
      "4642.481948852539", "5023.572692871094", "5155.821723937988", "5350.3723220825195", "5236.997489929199",
      "4910.097717285156", "4507.608840942383", "4659.80500793457", "5354.878845214844", "4945.796455383301",
      "6459.080368041992", "4390.493583679199", "6545.758262634277", "6922.801231384277", "6023.452911376953",
      "6812.107475280762", "6368.713348388672", "6381.748748779297", "5631.245086669922", "4976.192253112793",
      "6541.463027954102", "5983.8513107299805", "5967.189498901367", "5567.139289855957", "4863.5944747924805",
      "4681.164360046387", "6122.321441650391", "5410.308860778809", "4846.676376342773", "5333.872688293457",
      "5013.053741455078", "4836.85563659668", "5264.486434936523", "4581.821243286133", "4680.233596801758",
      "4771.363662719727", "5038.354717254639", "4816.808464050293", "4684.095504760742", "5023.663467407227",
      "5889.72257232666", "4984.973915100098", "5664.220512390137", "5572.653915405273", "5537.123138427734",
      "5980.422874450684", "6243.834693908691", "5372.147285461426", "5690.728981018066", "5827.796455383301",
      "6141.0769119262695", "6082.3237228393555", "5678.771339416504", "6814.467971801758", "6626.151596069336",
      "5833.2095947265625", "4679.222328186035", "5367.9403076171875", "5410.445640563965", "5689.197135925293",
      "5240.5018310546875", "4790.912239074707", "4992.670921325684", "4796.888023376465", "5479.439590454102",
      "5506.567192077637", "4743.144546508789", "4913.282669067383", "4723.869743347168"
  };

  public static final DateTime skippedDay = new DateTime("2011-01-21T00:00:00.000Z");

  public static final QuerySegmentSpec firstToThird = new MultipleIntervalSegmentSpec(
      Arrays.asList(new Interval("2011-04-01T00:00:00.000Z/2011-04-03T00:00:00.000Z"))
  );
  public static final QuerySegmentSpec fullOnInterval = new MultipleIntervalSegmentSpec(
      Arrays.asList(new Interval("1970-01-01T00:00:00.000Z/2020-01-01T00:00:00.000Z"))
  );

  @SuppressWarnings("unchecked")
  public static Collection<?> makeQueryRunners(
      QueryRunnerFactory factory
  )
      throws IOException
  {
    final Index testIndex = TestIndex.getTestIndex();
    final IncrementalIndex rtIndex = TestIndex.getIncrementalTestIndex();
    final MMappedIndex persistedRTIndex = TestIndex.getMMappedTestIndex();
    final MMappedIndex mergedRT = TestIndex.mergedRealtimeIndex();
    return Arrays.asList(
        new Object[][]{
            {
                makeQueryRunner(factory, new IndexStorageAdapter(testIndex))
            },
            {
                makeQueryRunner(factory, new MMappedIndexStorageAdapter(MMappedIndex.fromIndex(testIndex)))
            },
            {
                makeQueryRunner(factory, new IncrementalIndexStorageAdapter(rtIndex))
            },
            {
                makeQueryRunner(factory, new MMappedIndexStorageAdapter(persistedRTIndex))
            },
            {
                makeQueryRunner(factory, new MMappedIndexStorageAdapter(mergedRT))
            }
        }
    );
  }

  private static <T> QueryRunner<T> makeQueryRunner(
      QueryRunnerFactory<T, Query<T>> factory,
      StorageAdapter adapter
  )
  {
    return new FinalizeResultsQueryRunner<T>(
        factory.createRunner(adapter),
        factory.getToolchest()
    );
  }
}
