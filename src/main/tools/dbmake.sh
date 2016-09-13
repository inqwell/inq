#!/bin/ksh

#USAGE="Usage: createFiles.bat <ACTION=sql|pkey|filter|schema|static|data> <PKG=xylinq|inq> <INQDB=mysql|oracle>"

validateList()
{
  typeset val="$1"
  typeset list="$2"
  typeset msg="$3"

  if [ "$msg" = "" ]
  then
    msg="Not a valid value"
  fi

  typeset v
  for v in $list
  do
    if [ "$v" = "$val" ]
    then
      return 0
    fi
  done

  echo "$val : $msg" 1>&2
  exit 1
}

##
## Validate the variables set by command line parsing
## represent allowed combinations. Exit with usage if not
##
validateArgs()
{
  if [ "${ACTION}" = "" ]
  then
    echo "Specify action with -a sql|pkey|filter|schema|static|data" 1>&2
    exit 1
  fi

  if [ "${VENDOR}" = "" ]
  then
    echo "DB vendor not specified" 1>&2
    exit 1
  fi

  if [ "$ACTION" = "pkey" -o "$ACTION" = "filter" ]
  then
    if [ "$TARGET" = "" -o "$IN" = "" ]
    then
      echo "Must specify a target with -t AND metadata with -i for pkey and filter" 1>&2
      exit 1
    fi
  fi

  if [ "$ACTION" = "sql" -o "$ACTION" = "static" ]
  then
    if [ "$SQL" = "" -o "$IN" = "" ]
    then
      echo "Must specify an SQL directory with -s AND metadata with -i for sql and static" 1>&2
      exit 1
    fi
  fi

  if [ "$ACTION" = "schema" -a "$SQL" = "" ]
  then
    echo "Must specify an SQL directory with -s for sql and schema" 1>&2
    exit 1
  fi
     
  validateList "$VENDOR" "mysql oracle" "Not a valid DB vendor"
  validateList "$ACTION" "sql pkey filter schema static data clean" "Not a valid action"
}

##
## Process the XML meta data files to make individual
## SQL files for each table. Also contains the DB primary
## key. There are no other keys at present.
##
makeSql()
{
  mkdir -p ${SQL}/${VENDOR} 2> /dev/null

  python $PYTHONPATH/xml_to_${VENDOR}_table_file.py \
      --in_dir=${IN} \
      --out_dir=${SQL}/${VENDOR} \
      --template=$PYTHONPATH/${VENDOR}_table_file_template.txt
  if [ $? -ne 0 ]
  then
    exit 1
  fi
  echo "Created $ACTION files into ${SQL}/${VENDOR}"
}

##
## Process all the sql output from makeSql into
## a aingle file to create the entire schema
##
makeSchema()
{
  python $PYTHONPATH/setup_${VENDOR}_XySchema.py \
      --dirs=${SQL}/${VENDOR} \
      --test > ${SQL}/${VENDOR}_schema.sql
  if [ $? -ne 0 ]
  then
    exit 1
  fi

  echo "Created $ACTION file to ${SQL}/${VENDOR}_schema.sql"
}

##
## Make the pkey.sql files defining the select,
## update and delete SQL for each typedef
##
makePkey()
{
  typeset outdir=${TARGET}/$VENDOR
  python $PYTHONPATH/xml_to_${VENDOR}_pkey_file.py  --in_dir=${IN} --out_dir=$outdir
  if [ $? -ne 0 ]
  then
    exit 1
  fi

  echo "Created $ACTION files into $outdir"
}

##
## Make the <key>.sql files defining other where clauses
## for specific keys
##
makeFilter()
{
  typeset outdir=${TARGET}/$VENDOR

  python $PYTHONPATH/meta_to_${VENDOR}_filter_files.py \
      --in_dir=${IN}/metafilters \
      --out_dir=$outdir
  if [ $? -ne 0 ]
  then
    exit 1
  fi

  echo "Created $ACTION files into $outdir"
}

##
## Create SQL files to initialise the schema
## with test data eg accounts, swaps etc
##
makeStatic()
{
  mkdir -p ${SQL}/${VENDOR} 2> /dev/null

  python $PYTHONPATH/meta_to_db_data.py \
      --mode=insert_file \
      --meta_data_dir=${IN}/static \
      --xml_dirs=${IN} \
      --out=${SQL}/${VENDOR}_teststatic.sql \
      --db=$VENDOR --sep=";\n commit;"
  if [ $? -ne 0 ]
  then
    exit 1
  fi

  echo "Created $ACTION staticdata file to ${SQL}/${VENDOR}_teststatic.sql"
}

makeData()
{
  ## Note - not used
  python $PYTHONPATH/meta_to_db_data.py \
      --mode=insert_file \
      --meta_data_dir=db/data \
      --xml_dirs=db \
      --out=${SQL}/${VENDOR}_testdata.sql \
      --db=$VENDOR --sep=";\n commit;"
  if [ $? -ne 0 ]
  then
    exit 1
  fi

  echo Created $ACTION mktdata file to ${SQL}/${VENDOR}_testdata.sql
}


USAGE=$'[-?\n@(#)$Id: 1.0]'
USAGE+="[-author?tom.sanders@inqwell.com]"
USAGE+="[+NAME?$0 -- Create for specific database vendor]"
USAGE+="[+DESCRIPTION?Convert metadata to a vendor database and create data loads]"
#       -a, --action action to perform
USAGE+="[a:action?action]:[The action to perform, one of sql|pkey|filter|schema|static]"
#       -v, --vendor db vendor
USAGE+="[v:vendor?vendor]:[The database vendor, presently mysql|oracle]"
#       -t, --target target directory
USAGE+="[t:target?target]:[The target directory for pkey and filter results]"
#       -s, --sql where to place generated schema sql
USAGE+="[s:sql?SQL]:[Where to place generated schema sql]"
#       -i, --in where xml metadata is
USAGE+="[i:in?input_dir]:[Directory where metadata is]"
#       -h, --man
USAGE+="[h:help?Help]"

while getopts "${USAGE}" OPTCHAR
do
        case ${OPTCHAR} in
        a)      ACTION=${OPTARG}
                ;;
        v)      VENDOR=${OPTARG}
                ;;
        i)      IN=${OPTARG}
                ;;
        s)      SQL=${OPTARG}
                ;;
        t)      TARGET=${OPTARG}
                ;;
        h)      $0 --man
                exit 0
                ;;
        esac
done

validateArgs

PYTHONPATH=inq/tools/dbpy
if [ "$TARGET" != "" ]
then
  mkdir -p ${TARGET} 2> /dev/null
fi

#timestamp=`date "+%F %T"`
#echo $timestamp: Processing... $*

case ${ACTION} in
  sql)    makeSql
          ;;
  pkey)   makePkey
          ;;
  filter) makeFilter
          ;;
  schema) makeSchema
          ;;
  static) makeStatic
          ;;
  data)   makeData
          ;;
esac
exit $?

