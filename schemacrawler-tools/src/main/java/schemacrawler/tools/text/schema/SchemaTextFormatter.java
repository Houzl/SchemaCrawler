/* 
 *
 * SchemaCrawler
 * http://sourceforge.net/projects/schemacrawler
 * Copyright (c) 2000-2012, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */

package schemacrawler.tools.text.schema;


import java.util.Locale;

import schemacrawler.schema.ActionOrientationType;
import schemacrawler.schema.CheckConstraint;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ColumnMap;
import schemacrawler.schema.ConditionTimingType;
import schemacrawler.schema.EventManipulationType;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnMap;
import schemacrawler.schema.ForeignKeyUpdateRule;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.Privilege;
import schemacrawler.schema.Privilege.Grant;
import schemacrawler.schema.Procedure;
import schemacrawler.schema.ProcedureColumn;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schema.Trigger;
import schemacrawler.schema.View;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.tools.analysis.associations.SimpleWeakAssociationsCollector;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.text.base.BaseTabularFormatter;
import schemacrawler.tools.text.utility.Alignment;
import schemacrawler.tools.text.utility.TextFormattingHelper.DocumentHeaderType;
import schemacrawler.tools.traversal.SchemaTraversalHandler;
import sf.util.Utility;

/**
 * Text formatting of schema.
 * 
 * @author Sualeh Fatehi
 */
final class SchemaTextFormatter
  extends BaseTabularFormatter<SchemaTextOptions>
  implements SchemaTraversalHandler
{

  private static String negate(final boolean positive, final String text)
  {
    String textValue = text;
    if (!positive)
    {
      textValue = "not " + textValue;
    }
    return textValue;
  }

  private final boolean isVerbose;
  private final boolean isList;

  /**
   * Text formatting of schema.
   * 
   * @param schemaTextDetailType
   *        Types for text formatting of schema
   * @param options
   *        Options for text formatting of schema
   * @param outputOptions
   *        Options for text formatting of schema
   * @throws SchemaCrawlerException
   *         On an exception
   */
  SchemaTextFormatter(final SchemaTextDetailType schemaTextDetailType,
                      final SchemaTextOptions options,
                      final OutputOptions outputOptions)
    throws SchemaCrawlerException
  {
    super(options,
          schemaTextDetailType == SchemaTextDetailType.details,
          outputOptions);
    isVerbose = schemaTextDetailType
      .isGreaterThanOrEqualTo(SchemaTextDetailType.details);
    isList = schemaTextDetailType == SchemaTextDetailType.list;
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handle(schemacrawler.schema.ColumnDataType)
   */
  @Override
  public void handle(final ColumnDataType columnDataType)
    throws SchemaCrawlerException
  {
    if (printVerboseDatabaseInfo && isVerbose)
    {
      out.print(formattingHelper.createObjectStart(""));
      printColumnDataType(columnDataType);
      out.print(formattingHelper.createObjectEnd());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handle(schemacrawler.schema.Procedure)
   */
  @Override
  public void handle(final Procedure procedure)
  {
    final String procedureTypeDetail = "procedure, " + procedure.getType();
    final String procedureName;
    if (options.isShowUnqualifiedNames())
    {
      procedureName = procedure.getName();
    }
    else
    {
      procedureName = procedure.getFullName();
    }
    final String procedureType = "[" + procedureTypeDetail + "]";

    if (isList)
    {
      out.println(formattingHelper.createNameValueRow(procedureName,
                                                      procedureType,
                                                      Alignment.right));
    }
    else
    {
      out.print(formattingHelper.createObjectStart(""));
      out.println(formattingHelper.createNameRow(procedureName,
                                                 procedureType,
                                                 true));

      printProcedureColumns(procedure.getColumns());
      printDefinition("definition", "", procedure.getDefinition());

      if (isVerbose)
      {
        printDefinition("remarks", "", procedure.getRemarks());
      }

      out.println(formattingHelper.createObjectEnd());
    }

    out.flush();

  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handle(schemacrawler.schema.Synonym)
   */
  @Override
  public void handle(final Synonym synonym)
  {
    final String synonymName;
    if (options.isShowUnqualifiedNames())
    {
      synonymName = synonym.getName();
    }
    else
    {
      synonymName = synonym.getFullName();
    }
    final String synonymType = "[synonym]";

    if (isList)
    {
      out.println(formattingHelper.createNameValueRow(synonymName,
                                                      synonymType,
                                                      Alignment.right));
    }
    else
    {
      out.print(formattingHelper.createObjectStart(""));
      out.println(formattingHelper
        .createNameRow(synonymName, synonymType, true));

      final String referencedObjectName;
      if (options.isShowUnqualifiedNames())
      {
        referencedObjectName = synonym.getReferencedObject().getName();
      }
      else
      {
        referencedObjectName = synonym.getReferencedObject().getFullName();
      }
      out.println(formattingHelper.createDetailRow("",
                                                   synonym.getName()
                                                       + formattingHelper
                                                         .createArrow()
                                                       + referencedObjectName,
                                                   ""));

      if (isVerbose)
      {
        printDefinition("remarks", "", synonym.getRemarks());
      }

      out.println(formattingHelper.createObjectEnd());
    }

    out.flush();

  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handle(schemacrawler.schema.Table)
   */
  @Override
  public void handle(final Table table)
  {
    final String tableName;
    if (options.isShowUnqualifiedNames())
    {
      tableName = table.getName();
    }
    else
    {
      tableName = table.getFullName();
    }
    final String tableType = "[" + table.getType() + "]";

    if (isList)
    {
      out.println(formattingHelper.createNameValueRow(tableName,
                                                      tableType,
                                                      Alignment.right));
    }
    else
    {
      out.print(formattingHelper.createObjectStart(""));
      out.println(formattingHelper.createNameRow(tableName, tableType, true));

      final Column[] columns = table.getColumns();
      printTableColumns(columns);

      printPrimaryKey(table.getPrimaryKey());
      printForeignKeys(table.getName(), table.getForeignKeys());
      if (isVerbose)
      {
        printWeakAssociations(table);
      }
      printIndices(table.getIndices());
      if (isVerbose)
      {
        printCheckConstraints(table.getCheckConstraints());
        printPrivileges(table.getPrivileges());
        printTriggers(table.getTriggers());
      }
      if (table instanceof View)
      {
        final View view = (View) table;
        printDefinition("definition", "", view.getDefinition());
      }
      if (isVerbose)
      {
        final String tableRemarks = table.getRemarks();
        boolean hasColumnRemarks = false;
        for (final Column column: columns)
        {
          final String remarks = column.getRemarks();
          if (!Utility.isBlank(remarks))
          {
            hasColumnRemarks = true;
            break;
          }
        }

        if (Utility.isBlank(tableRemarks) && hasColumnRemarks)
        {
          out.println(formattingHelper.createEmptyRow());
          out.println(formattingHelper.createNameRow("", "[remarks]", false));
        }
        else
        {
          printDefinition("remarks", "", tableRemarks);
        }
        for (final Column column: columns)
        {
          final String remarks = column.getRemarks();
          if (!Utility.isBlank(remarks))
          {
            out.println(formattingHelper.createDetailRow("",
                                                         column.getName(),
                                                         remarks));
          }
        }
      }
      out.println(formattingHelper.createObjectEnd());
    }
    out.flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleColumnDataTypesEnd()
   */
  @Override
  public void handleColumnDataTypesEnd()
  {
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleColumnDataTypesStart()
   */
  @Override
  public void handleColumnDataTypesStart()
  {
    if (printVerboseDatabaseInfo && isVerbose)
    {
      out.println(formattingHelper.createHeader(DocumentHeaderType.subTitle,
                                                "Data Types"));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleProceduresEnd()
   */
  @Override
  public void handleProceduresEnd()
    throws SchemaCrawlerException
  {
    if (isList)
    {
      out.print(formattingHelper.createObjectEnd());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleProceduresStart()
   */
  @Override
  public void handleProceduresStart()
    throws SchemaCrawlerException
  {
    out.println(formattingHelper.createHeader(DocumentHeaderType.subTitle,
                                              "Procedures"));

    if (isList)
    {
      out.print(formattingHelper.createObjectStart(""));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleSynonymsEnd()
   */
  @Override
  public void handleSynonymsEnd()
    throws SchemaCrawlerException
  {
    if (isList)
    {
      out.print(formattingHelper.createObjectEnd());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleSynonymsStart()
   */
  @Override
  public void handleSynonymsStart()
    throws SchemaCrawlerException
  {
    out.println(formattingHelper.createHeader(DocumentHeaderType.subTitle,
                                              "Synonyms"));

    if (isList)
    {
      out.print(formattingHelper.createObjectStart(""));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleTablesEnd()
   */
  @Override
  public void handleTablesEnd()
    throws SchemaCrawlerException
  {
    if (isList)
    {
      out.print(formattingHelper.createObjectEnd());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see schemacrawler.tools.traversal.SchemaTraversalHandler#handleTablesStart()
   */
  @Override
  public void handleTablesStart()
    throws SchemaCrawlerException
  {
    out.println(formattingHelper.createHeader(DocumentHeaderType.subTitle,
                                              "Tables"));

    if (isList)
    {
      out.print(formattingHelper.createObjectStart(""));
    }
  }

  private void printCheckConstraints(final CheckConstraint[] constraints)
  {
    for (final CheckConstraint constraint: constraints)
    {
      if (constraint != null)
      {
        String constraintName = "";
        if (!options.isHideConstraintNames())
        {
          constraintName = constraint.getName();
        }
        printDefinition("check constraint",
                        constraintName,
                        constraint.getDefinition());
      }
    }
  }

  private void printColumnDataType(final ColumnDataType columnDataType)
  {
    final String databaseSpecificTypeName;
    if (options.isShowUnqualifiedNames())
    {
      databaseSpecificTypeName = columnDataType.getName();
    }
    else
    {
      databaseSpecificTypeName = columnDataType.getFullName();
    }
    final String typeName = columnDataType.getTypeName();
    final String userDefined = negate(columnDataType.isUserDefined(),
                                      "user defined");
    final String nullable = negate(columnDataType.isNullable(), "nullable");
    final String autoIncrementable = negate(columnDataType.isAutoIncrementable(),
                                            "auto-incrementable");
    String definedWith = "defined with ";
    if (columnDataType.getCreateParameters() == null)
    {
      definedWith = definedWith + "no parameters";
    }
    else
    {
      definedWith = definedWith + columnDataType.getCreateParameters();
    }
    out.println(formattingHelper.createNameRow(databaseSpecificTypeName,
                                               "[data type]",
                                               false));
    out.println(formattingHelper.createDetailRow("", "based on", typeName));
    out.println(formattingHelper.createDescriptionRow(userDefined));
    out.println(formattingHelper.createDescriptionRow(definedWith));
    out.println(formattingHelper.createDescriptionRow(nullable));
    out.println(formattingHelper.createDescriptionRow(autoIncrementable));
    out.println(formattingHelper.createDescriptionRow(columnDataType
      .getSearchable().toString()));
  }

  private void printColumnPairs(final String tableName,
                                final ColumnMap... columnPairs)
  {
    for (final ColumnMap columnPair: columnPairs)
    {
      final Column pkColumn;
      final Column fkColumn;
      final String pkColumnName;
      final String fkColumnName;
      pkColumn = columnPair.getPrimaryKeyColumn();
      fkColumn = columnPair.getForeignKeyColumn();
      if (pkColumn.getParent().getName().equals(tableName))
      {
        pkColumnName = pkColumn.getName();
      }
      else if (options.isShowUnqualifiedNames())
      {
        pkColumnName = pkColumn.getShortName();
      }
      else
      {
        pkColumnName = pkColumn.getFullName();
      }
      if (fkColumn.getParent().getName().equals(tableName))
      {
        fkColumnName = fkColumn.getName();
      }
      else if (options.isShowUnqualifiedNames())
      {
        fkColumnName = fkColumn.getShortName();
      }
      else
      {
        fkColumnName = fkColumn.getFullName();
      }
      String keySequenceString = "";
      if (columnPair instanceof ForeignKeyColumnMap
          && options.isShowOrdinalNumbers())
      {
        final int keySequence = ((ForeignKeyColumnMap) columnPair)
          .getKeySequence();
        keySequenceString = String.format("%2d", keySequence);
      }
      out.println(formattingHelper.createDetailRow(keySequenceString,
                                                   pkColumnName
                                                       + formattingHelper
                                                         .createArrow()
                                                       + fkColumnName,
                                                   ""));
    }
  }

  private void printDefinition(final String heading,
                               final String name,
                               final String definition)
  {
    if (Utility.isBlank(definition))
    {
      return;
    }
    final String definitionName;
    if (Utility.isBlank(name))
    {
      definitionName = "";
    }
    else
    {
      definitionName = name;
    }
    out.println(formattingHelper.createEmptyRow());
    out.println(formattingHelper.createNameRow(definitionName, "[" + heading
                                                               + "]", false));
    out.println(formattingHelper.createDefinitionRow(definition));
  }

  private void printForeignKeys(final String tableName,
                                final ForeignKey[] foreignKeys)
  {
    for (final ForeignKey foreignKey: foreignKeys)
    {
      if (foreignKey != null)
      {
        final String name = foreignKey.getName();

        String updateRuleString = "";
        final ForeignKeyUpdateRule updateRule = foreignKey.getUpdateRule();
        if (updateRule != null && updateRule != ForeignKeyUpdateRule.unknown)
        {
          updateRuleString = ", on update " + updateRule.toString();
        }

        String deleteRuleString = "";
        final ForeignKeyUpdateRule deleteRule = foreignKey.getDeleteRule();
        if (deleteRule != null && deleteRule != ForeignKeyUpdateRule.unknown)
        {
          deleteRuleString = ", on delete " + deleteRule.toString();
        }

        final String ruleString;
        if (updateRule == deleteRule
            && updateRule != ForeignKeyUpdateRule.unknown)
        {
          ruleString = ", with " + deleteRule.toString();
        }
        else
        {
          ruleString = updateRuleString + deleteRuleString;
        }

        out.println(formattingHelper.createEmptyRow());

        String fkName = "";
        if (!options.isHideForeignKeyNames())
        {
          fkName = name;
        }
        final String fkDetails = "[foreign key" + ruleString + "]";
        out.println(formattingHelper.createNameRow(fkName, fkDetails, false));
        final ForeignKeyColumnMap[] columnPairs = foreignKey.getColumnPairs();
        printColumnPairs(tableName, columnPairs);
      }
    }
  }

  private void printIndices(final Index[] indices)
  {
    for (final Index index: indices)
    {
      if (index != null)
      {
        out.println(formattingHelper.createEmptyRow());

        String indexName = "";
        if (!options.isHideIndexNames())
        {
          indexName = index.getName();
        }
        final IndexType indexType = index.getType();
        String indexTypeString = "";
        if (indexType != IndexType.unknown && indexType != IndexType.other)
        {
          indexTypeString = indexType.toString() + " ";
        }
        final String indexDetails = "[" + (index.isUnique()? "": "non-")
                                    + "unique " + indexTypeString + "index]";
        out.println(formattingHelper.createNameRow(indexName,
                                                   indexDetails,
                                                   false));
        printTableColumns(index.getColumns());
      }
    }
  }

  private void printPrimaryKey(final Index primaryKey)
  {
    if (primaryKey != null)
    {
      final String name = primaryKey.getName();
      out.println(formattingHelper.createEmptyRow());

      String pkName = "";
      if (!options.isHidePrimaryKeyNames())
      {
        pkName = name;
      }
      if (Utility.isBlank(pkName))
      {
        pkName = "";
      }
      out.println(formattingHelper
        .createNameRow(pkName, "[primary key]", false));
      printTableColumns(primaryKey.getColumns());
    }
  }

  private void printPrivileges(final Privilege<?>[] privileges)
  {

    for (final Privilege<?> privilege: privileges)
    {
      if (privilege != null)
      {
        out.println(formattingHelper.createEmptyRow());
        out.println(formattingHelper.createNameRow(privilege.getName(),
                                                   "[privilege]",
                                                   false));
        for (final Grant grant: privilege.getGrants())
        {
          final String grantedFrom = grant.getGrantor()
                                     + formattingHelper.createArrow()
                                     + grant.getGrantee()
                                     + (grant.isGrantable()? " (grantable)": "");
          out.println(formattingHelper.createDetailRow("", grantedFrom, ""));
        }
      }
    }
  }

  private void printProcedureColumns(final ProcedureColumn[] columns)
  {
    for (final ProcedureColumn column: columns)
    {
      final String columnTypeName;
      if (options.isShowStandardColumnTypeNames())
      {
        columnTypeName = column.getType().getTypeName();
      }
      else
      {
        columnTypeName = column.getType().getDatabaseSpecificTypeName();
      }
      final StringBuilder columnType = new StringBuilder();
      columnType.append(columnTypeName).append(column.getWidth());
      if (column.getProcedureColumnType() != null)
      {
        columnType.append(", ").append(column.getProcedureColumnType()
          .toString());
      }

      String ordinalNumberString = "";
      if (options.isShowOrdinalNumbers())
      {
        ordinalNumberString = String.valueOf(column.getOrdinalPosition() + 1);
      }
      out.println(formattingHelper.createDetailRow(ordinalNumberString,
                                                   column.getName(),
                                                   columnType.toString()));
    }
  }

  private void printTableColumns(final Column[] columns)
  {
    for (final Column column: columns)
    {
      final String columnName = column.getName();

      final String columnDetails;
      if (column instanceof IndexColumn)
      {
        columnDetails = ((IndexColumn) column).getSortSequence().name();
      }
      else
      {
        String columnTypeName = column.getType().getDatabaseSpecificTypeName();
        if (options.isShowStandardColumnTypeNames())
        {
          columnTypeName = column.getType().getTypeName();
        }
        final String columnType = columnTypeName + column.getWidth();
        final String nullable = column.isNullable()? "": " not null";
        columnDetails = columnType + nullable;
      }

      String ordinalNumberString = "";
      if (options.isShowOrdinalNumbers())
      {
        ordinalNumberString = String.valueOf(column.getOrdinalPosition());
      }
      out.println(formattingHelper.createDetailRow(ordinalNumberString,
                                                   columnName,
                                                   columnDetails));
    }
  }

  private void printTriggers(final Trigger[] triggers)
  {
    for (final Trigger trigger: triggers)
    {
      if (trigger != null)
      {
        String timing = "";
        final ConditionTimingType conditionTiming = trigger
          .getConditionTiming();
        final EventManipulationType eventManipulationType = trigger
          .getEventManipulationType();
        if (conditionTiming != null
            && conditionTiming != ConditionTimingType.unknown
            && eventManipulationType != null
            && eventManipulationType != EventManipulationType.unknown)
        {
          timing = ", " + conditionTiming + " " + eventManipulationType;
        }
        String orientation = "";
        if (trigger.getActionOrientation() != null
            && trigger.getActionOrientation() != ActionOrientationType.unknown)
        {
          orientation = ", per " + trigger.getActionOrientation();
        }
        String triggerType = "[trigger" + timing + orientation + "]";
        triggerType = triggerType.toLowerCase(Locale.ENGLISH);
        final String actionCondition = trigger.getActionCondition();
        final String actionStatement = trigger.getActionStatement();
        out.println(formattingHelper.createEmptyRow());

        final String triggerName = trigger.getName();
        out.println(formattingHelper.createNameRow(triggerName,
                                                   triggerType,
                                                   false));

        if (!Utility.isBlank(actionCondition))
        {
          out.println(formattingHelper.createDescriptionRow(actionCondition));
        }
        if (!Utility.isBlank(actionStatement))
        {
          out.println(formattingHelper.createDescriptionRow(actionStatement));
        }
      }
    }
  }

  private void printWeakAssociations(final Table table)
  {
    final String tableName = table.getName();
    final ColumnMap[] weakAssociations = SimpleWeakAssociationsCollector
      .getWeakAssociations(table);
    for (final ColumnMap weakAssociation: weakAssociations)
    {
      out.println(formattingHelper.createEmptyRow());
      out.println(formattingHelper.createNameRow("",
                                                 "[weak association]",
                                                 false));
      printColumnPairs(tableName, weakAssociation);
    }
  }

}
