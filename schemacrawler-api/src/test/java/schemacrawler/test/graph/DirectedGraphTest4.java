package schemacrawler.test.graph;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import schemacrawler.test.utility.TestName;
import sf.util.graph.DirectedGraph;

/**
 * Exercises from
 * https://algocoding.wordpress.com/2015/04/02/detecting-cycles-in-a-
 * directed-graph-with-dfs-python/
 */
public class DirectedGraphTest4
  extends GraphTestBase
{

  @Rule
  public TestName testName = new TestName();

  @Test
  public void test1()
    throws Exception
  {
    final DirectedGraph<Integer> graph = new DirectedGraph<Integer>(testName
      .currentMethodFullName())
    {
      {
        addEdge(0, 1);
        addEdge(0, 2);
        addEdge(2, 3);
        addEdge(3, 4);
        addEdge(4, 2);
      }
    };

    assertTrue(containsCycleSimple(graph));
    assertTrue(containsCycleTarjan(graph));
  }

  @Test
  public void test2()
    throws Exception
  {
    final DirectedGraph<Integer> graph = new DirectedGraph<Integer>(testName
      .currentMethodFullName())
    {
      {
        addVertex(0);
        addVertex(1);
        addVertex(2);
        addVertex(3);
      }
    };

    assertFalse(containsCycleSimple(graph));
    assertFalse(containsCycleTarjan(graph));
  }

  @Test
  public void test3()
    throws Exception
  {
    final DirectedGraph<Integer> graph = new DirectedGraph<Integer>(testName
      .currentMethodFullName())
    {
      {
        addVertex(0);
        addEdge(1, 2);
        addEdge(3, 4);
        addEdge(4, 5);
        addEdge(5, 3);
      }
    };

    assertTrue(containsCycleSimple(graph));
    assertTrue(containsCycleTarjan(graph));
  }

  @Test
  public void test4()
    throws Exception
  {
    final DirectedGraph<Integer> graph = new DirectedGraph<Integer>(testName
      .currentMethodFullName())
    {
      {
        addEdge(0, 1);
        addEdge(0, 2);
        addEdge(1, 3);
        addEdge(1, 4);
        addEdge(5, 6);
        addEdge(5, 7);
      }
    };

    assertFalse(containsCycleSimple(graph));
    assertFalse(containsCycleTarjan(graph));
  }

}
