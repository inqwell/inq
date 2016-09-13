#!/bin/ksh
#
# Private & Confidential Copyright  Inqwell Ltd 2004-2016.
# All rights reserved.
#

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

validateArgs()
{
  if [ "${ACTION}" = "" ]
  then
    echo "Specify action with -a create|loadtest" 1>&2
    exit 1
  fi

  if [ "${VENDOR}" = "" ]
  then
    echo "DB vendor not specified" 1>&2
    exit 1
  fi

  if [ "${USER}" = "" ]
  then
    echo "DB user not specified" 1>&2
    exit 1
  fi

  if [ "${PASSWORD}" = "" ]
  then
    echo "DB password not specified" 1>&2
    exit 1
  fi

  if [ "$DBNAME" = "" ]
  then
    echo "Must specify a database name with -n" 1>&2
    exit 1
  fi

  validateList "$VENDOR" "mysql oracle" "Not a valid DB vendor"
  validateList "$ACTION" "create loadtest" "Not a valid action"
}

createDb()
{
  echo "Processing schema...."
  if [ "$VENDOR" = "mysql" ]
  then
    # Create database via inline commands so that we can control the db name.
    $MYSQL << !EOF
drop database if exists $DBNAME;
create database $DBNAME;
grant all on $DBNAME.* to 'inq'@'%' identified by 'inq123';
grant all on $DBNAME.* to 'inq'@'localhost' identified by 'inq123';
grant all on $DBNAME.* to 'inq'@'localhost.localdomain' identified by 'inq123';
!EOF
    $MYSQL $DBNAME < inq/app/inq/db/target/mysql_schema.sql
  elif [ "$VENDOR" = "oracle" ]
  then
    echo "oracle is TODO" 1>&2
    exit 1
  fi
}

loadTest()
{
  echo "Loading the test static data...."
  if [ "$VENDOR" = "mysql" ]
  then
    $MYSQL $DBNAME < inq/app/inq/db/target/mysql_teststatic.sql
  elif [ "$VENDOR" = "oracle" ]
  then
    echo "oracle is TODO" 1>&2
    exit 1
  fi
}

USAGE=$'[-?\n@(#)$Id: 1.0]'
USAGE+="[-author?tom.sanders@inqwell.com]"
USAGE+="[+NAME?$0 -- Create database for specific database vendor]"
USAGE+="[+DESCRIPTION?Create DB, load with test data"
#       -a, --action action to perform
USAGE+="[a:action?action]:[The action to perform, one of create|loadtest]"
#       -v, --vendor db vendor
USAGE+="[v:vendor?vendor]:[The database vendor, presently mysql|oracle]"
#       -n, --name database name 
USAGE+="[n:name?DB_Name]:[The database name]"
#       -u, --user SQL server user name
USAGE+="[u:user?User]:[SQL server user name]"
#       -p, --password SQL server password
USAGE+="[p:password?Password]:[SQL server password]"
#       -h, --man
USAGE+="[h:help?Help]"

while getopts "${USAGE}" OPTCHAR
do
        case ${OPTCHAR} in
        a)      ACTION=${OPTARG}
                ;;
        v)      VENDOR=${OPTARG}
                ;;
        s)      SQL=${OPTARG}
                ;;
        n)      DBNAME=${OPTARG}
                ;;
        u)      USER=${OPTARG}
                ;;
        p)      PASSWORD=${OPTARG}
                ;;
        h)      $0 --man
                exit 0
                ;;
        esac
done

validateArgs

MYSQL="mysql -u${USER} -p${PASSWORD} --local-infile "

case ${ACTION} in
  create)    createDb
             ;;
  loadtest)  loadTest
             ;;
esac

exit $?
