# SchemaCrawler - Diagram Example

## Description
The diagram example demonstrates the integration of SchemaCrawler with Graphviz.

## How to Run
1. Make sure that java is on your PATH

### Run With SQLite Database

2. Start a command shell in the diagram example directory 
3. Run `sqlite_diagram.cmd ..\..\_testdb\sc.db sc.pdf` (or `sqlite_diagram.sh  ../../_testdb/sc.db sc.pdf` on Unix) 

### Run With HyperSQL Database

2. Start the test database server by following instructions in the `_testdb/README.html` file
3. Start a command shell in the diagram example directory 
4. Run `diagram.cmd` (or `diagram.sh` on Unix) 

## How to Experiment
1. Try using grep options to include certain tables. For example, try using a command-line option of `-grepcolumns=.*\\.AUTHOR.*`
2. Try controlling display of foreign-key names, column ordinal numbers, and schema names by setting the 
   following properties in the SchemaCrawler configuration file, `config/schemacrawler.config.properties`. 

```           
schemacrawler.format.show_ordinal_numbers=true        
schemacrawler.format.hide_foreignkey_names=true
schemacrawler.format.show_unqualified_names=true
```    

3. Try using Graphviz command-line options by setting the following property in the SchemaCrawler configuration file, 
   `config/schemacrawler.config.properties`. 
    
```        
schemacrawler.graph.graphviz_opts=-Gdpi=300
```    

## SchemaCrawler Web Application

Take a look at the [SchemaCrawler Web Application](http://www.schemacrawler.com/webapp.html).
