package com.linglong.sql;

import com.linglong.sql.statement.SelectStatement;

import java.io.IOException;

/**
 * @author Stereo on 2021/3/12.
 */
public class SQLTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        String SQL = "SELECT (time + 1) as timeStamp, sum(DISTINCT(v.n)) as b, avg('v.s') as a, min(c) as c FROM \"enno\".\"two_day_only\".\"device_environmentcontrolequipment_events\",(SELECT * FROM TEST WHERE projectId = '6dec6a02-2afd-4cef-89c7-07af5283598c' and attrCode = 'temp' group by a,c,d order by a asc, d desc, c limit 1 offset 2) WHERE projectId = '6dec6a02-2afd-4cef-89c7-07af5283598c' and attrCode = 'temp' group by a,c,d order by a asc, d desc, c limit 1 offset 2";
        SQL = "select * from a where b in (1,2,3,4)";
        System.out.println(SQL);
        System.out.println();
        while (true) {
            long start = System.currentTimeMillis();
            LinglongSQLParseKernel kernel = new LinglongSQLParseKernel(SQL);
            SelectStatement selectStatement = (SelectStatement) kernel.parse();
            System.out.println(selectStatement);
            System.out.println("SQL解析损耗: " + (System.currentTimeMillis() - start) + "ms");
            Thread.sleep(1000L);
        }

        /*
        InfluxSQLParserEngine engine = new InfluxSQLParserEngine(SQL);
        InfluxSQLAST influxSQLAST = engine.parse();

        //select items
        SelectItemsExtractor selectItemsExtractor = new SelectItemsExtractor();
        Optional<SelectItemsSegment> selectItemsSegmentOptional = selectItemsExtractor.extract(influxSQLAST.getParserRuleContext());
        SelectItemsSegment selectItemsSegment = selectItemsSegmentOptional.get();
        System.out.println(selectItemsSegment);

        //table
        TableReferencesExtractor tableReferencesExtractor = new TableReferencesExtractor();
        Collection<TableSegment> tableSegments = tableReferencesExtractor.extract(influxSQLAST.getParserRuleContext());
        System.out.println(tableSegments);

        //where
        WhereExtractor whereExtractor = new WhereExtractor();
        Optional<WhereSegment> whereSegment = whereExtractor.extract(influxSQLAST.getParserRuleContext());
        System.out.println(whereSegment.get());

        //group by
        GroupByExtractor groupByExtractor = new GroupByExtractor();
        Optional<GroupBySegment> groupBySegmentOptional = groupByExtractor.extract(influxSQLAST.getParserRuleContext());
        System.out.println(groupBySegmentOptional.get());

        //order by
        OrderByExtractor orderByExtractor = new OrderByExtractor();
        Optional<OrderBySegment> orderBySegmentOptional = orderByExtractor.extract(influxSQLAST.getParserRuleContext());
        System.out.println(orderBySegmentOptional.get());

        //limit
        LimitExtractor limitExtractor = new LimitExtractor();
        Optional<LimitSegment> limitSegment = limitExtractor.extract(influxSQLAST.getParserRuleContext());
        System.out.println(limitSegment.get());
        */
    }
}
