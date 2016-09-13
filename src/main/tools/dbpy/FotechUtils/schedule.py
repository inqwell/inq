#!/usr/local/bin/python

import KBC.fotech;
from types import IntType, ListType, TupleType, StringType;
from Util.enumeration import Enumeration;
import time, re;


class Schedule:
    """
    Schedule provides a way of specifying when a job is to be active. The active
    time frame is specified in a way similar to cron. Valid ranges are provided as
    one of: a single integer, a list of integers, a wildcard or a cron formatted
    string.

    - A single integer matches that and only that value. For example if '0' is
      passed in as the dayofweek parameter then the Schedule will only be active
      on Mondays.

    - A list of integers match when the time is equal to any one of those values.
      So a dayofmonth parameter with value '[ 1, 2, 3, 4 ]' will only be active on
      the first four days of any month.

    - A wildcard is always matched. So a Schedule will WILDCARD as all its
      parameters will always be active.

    - A cron formatted string is either a wildcard (asterisk) or a comma separated
      list of values; these values can be either an integer or a range (two
      integers separated by a hyphen). Note, unlike the Python 'range' command
      the string ranges are inclusive, so "2-5" becomes: 2, 3, 4, 5.

    A given time must match all four parameters to be deemed active.

    Schedule.WeekDay is an enumeration that provides string access to the Weekday
    integer values.

    Schedule.WILDCARD is a parameter value that is always active.
    Schedule.NULL is a parameter value that is never active.

    Schedule.ALWAYS is a static instance of Schedule that is always active.
    Schedule.NEVER is the opposide of ALWAYS, it will never be active.   
    
    """

    WeekDay = Enumeration ( [ 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY' ] );

    validRangeMap = { 'minute' : range ( 60 ), 'hour' : range ( 24 ), 'dayofmonth' : range ( 1, 32 ), 'dayofweek' : range ( 7 ) };
    valueMangler = { 'dayofweek' : lambda value : [ ( element - 1 ) % 7 for element in value ] };
    integerRegEx = re.compile ( '^\d+$' );
    rangeRegEx = re.compile ( '^\d+\s*\-\s*\d+$' );
    wildcardRegEx = re.compile ( '^\s*\*\s*$' );

    def __init__ ( self, minute, hour, dayofmonth, dayofweek ):
        """
        Create a Schedule instance using the parameters provided. The parameters, and
        their valid ranges, are:
        
        minute     [0-59] - The minute component of the time.
        hour       [0-23] - The hour component of the time.
        dayofmonth [1-31] - The day of the month, unlike the other parameters this is
                            1-indexed.
        dayofweek  [0-6]  - The day of the week, started from Sunday.
        """
        self.minute = self.isParameterValid ( 'minute', minute );
        self.hour = self.isParameterValid ( 'hour', hour );
        self.dayofmonth = self.isParameterValid ( 'dayofmonth', dayofmonth );
        self.dayofweek = self.isParameterValid ( 'dayofweek', dayofweek );
        self.tupleLookup = ( self.minute, self.hour, self.dayofmonth, self.dayofweek );

    def isParameterValid ( self, paramtype, value ):
        # Check the top-level type of the value
        if not paramtype in Schedule.validRangeMap: raise KeyError, 'Value type "%s" does not have a mapping' % paramtype;
        if not type ( value ) in ( IntType, StringType, ListType, TupleType ):
            raise TypeError, 'Value "%s" must be a single integer, a list of integers or a cron format string';
        # Convert cron strings to a list of values or a wildcard
        if type ( value ) == StringType: value = self.parseCronString ( value );
        # Handle wildcards or single integer values
        if type ( value ) == IntType:
            if value == Schedule.WILDCARD:
                value = Schedule.validRangeMap [ paramtype ];
            else:
                value = [ value ];
        # Check the type and range of the value's elements
        for element in value:
            if type ( element ) != IntType: raise TypeError, 'Value "%s", if a list, must contain only integers' % value;
            if not element in Schedule.validRangeMap [ paramtype ]:
                raise ValueError, 'Value "%s", %d, is not in the valid range' % ( paramtype, element )
        # Mangle the value if necessary
        if paramtype in Schedule.valueMangler:
            value = Schedule.valueMangler [ paramtype ] ( value );
        return frozenset ( value );

    def parseEpochTime ( self, seconds ):
        timetuple = time.localtime ( seconds );
        return timetuple.tm_min, timetuple.tm_hour, timetuple.tm_mday, timetuple.tm_wday;

    def generatePeriods ( self, seconds, lastrun = None ):
        if lastrun is None:
            return [ self.parseEpochTime ( seconds ) ];
        return [ self.parseEpochTime ( when ) for when in range ( int ( lastrun ) + 60, int ( seconds ) + 60, 60 ) ];

    def parseCronString ( self, string ):
        if Schedule.wildcardRegEx.match ( string ): return Schedule.WILDCARD;
        values = [ ];
        for element in string.split ( ',' ):
            element = element.strip ( );
            if not element: continue;
            if Schedule.integerRegEx.match ( element ):
                values.append ( int ( element ) );
            elif Schedule.rangeRegEx.match ( element ):
                begin, end = [ int ( number.strip ( ) ) for number in element.split ( '-' ) ];
                if end < begin: raise ValueError, 'Range in cron string "%s" goes backwards, %d < %d' % ( string, end, begin );
                values.extend ( range ( begin, end + 1 ) );
            else:
                raise ValueError, 'Unrecognised cron string element: "%s" in "%s"' % ( element, string );
        values.sort ( );
        return frozenset ( values );

    def isPeriodActive ( self, period ):
        assert ( len ( period ) == len ( self.tupleLookup ) );
        for index in range ( len ( period ) ):
            if not period [ index ] in self.tupleLookup [ index ]:
                return False;
        return True;

    def isActive ( self, seconds = None, lastrun = None ):
        """
        Takes the time to be checked as seconds since the epoch. Returns true if the
        time falls within Schedule's window, otherwise false.

        Takes the time in seconds (UNIX time) and, optionally, the time of the last
        run (also UNIX time). If there is no lasttime provided then the method returns
        true if seconds falls within the Schedule's window, otherwise false.

        If lasttime is provided a list of times is produced, one for each minute 
        where: lasttime < N <= seconds. If any of the times in the list are within
        the Schedule's window, then the Schedule is deemed active.
        """
        if seconds is None: seconds = time.time ( );
        periods = self.generatePeriods ( seconds, lastrun );
        for period in periods:
            if self.isPeriodActive ( period ):
                return True;
        return False;

        


Schedule.WILDCARD = -1;
Schedule.NULL = [ ];
Schedule.ALWAYS = Schedule ( Schedule.WILDCARD, Schedule.WILDCARD, Schedule.WILDCARD, Schedule.WILDCARD );
Schedule.NEVER  = Schedule ( Schedule.NULL, Schedule.NULL, Schedule.NULL, Schedule.NULL );
    

class CronSchedule ( Schedule ):
    """
    A wrapper class around Schedule. Provides exactly the same functionality as
    Schedule but can be created from a single string. This string contains the
    usual four fields (see Schedule) in the same order (minute, hour, day of month
    and day of week), white space separated. Like cron the wildcard character is
    an asterisk.

    For example:

    > schedule = CronSchedule ( '0 * * 0-4,6' );

    This creates a schedule that is active on the hour, every hour; on every day
    except Saturday (whose index is 5).
    """

    elementsRegEx = re.compile ( '^\s*(\*|[0-9-,]+)\s+(\*|[0-9-,]+)\s+(\*|[0-9-,]+)\s+(\*|[0-9-,]+)\s*$' );

    def __init__ ( self, string ):
        if type ( string ) != StringType: raise TypeError, 'CronSchedule can only be constructed from a string';
        matches = CronSchedule.elementsRegEx.match ( string );
        if not matches: raise ValueError, 'Cron string "%s" is not valid' % string;
        matches = matches.groups ( );
        Schedule.__init__ ( self, matches [ 0 ], matches [ 1 ], matches [ 2 ], matches [ 3 ] );
    
    
def StringToEpochTime ( string ):
    """ Converts a time string in the format 'YYYYMMDDhhmm' in to a Unix time integer. """
    return time.mktime ( time.strptime ( string, '%Y%m%d%H%M' ) );
    

class NMinutesSchedule ( Schedule ):
    """
    A quick helper that allows scheduling for every n minutes within the hour.  It doesn't cope properly
    with n not dividing into 60, or where n is larger than 30, so don't do it.
    """
    def __init__ ( self, n, offset = 0 ):
        if n < 0 or n > 30 or 60 % n != 0: raise Exception, 'n must divide into 60.';

        Schedule.__init__ ( self, range ( offset, 60, n ), Schedule.WILDCARD, Schedule.WILDCARD, Schedule.WILDCARD );



##############################################################################
# Test harness

if __name__ == '__main__':
    # Check the most basic ones
    assert ( Schedule.ALWAYS.isActive ( ) );
    assert ( not Schedule.NEVER.isActive ( ) );

    # Check the wildcard cron string
    assert ( Schedule ( '*', '*', '*', '*' ).isActive ( ) );
    assert ( not Schedule ( '', '', '', '' ).isActive ( ) );

    # Test against a single Schedule instance (first thirty minutes, of hours in the range 8->11 & 14->19, on a week day that isn't the first day of the month
    schedules = ( Schedule ( range ( 0, 30 ), range ( 8, 12 ) + range ( 14, 20 ), range ( 2, 32 ), range ( 1, 6 ) ),
                  Schedule ( '0-29', '8,9,10,11,14-19', '2-31', '1,2,3,4,5' ),
                  CronSchedule ( '0-29 8-11,14-19 2-31 1,2,3,4,5' ) );

    for schedule in schedules:
        assert (     schedule.isActive ( StringToEpochTime ( '200506201029' ) ) );    # Will work
        assert ( not schedule.isActive ( StringToEpochTime ( '200506201030' ) ) );    # Will fail, minute value is one over upper boundary
        assert ( not schedule.isActive ( StringToEpochTime ( '200506201229' ) ) );    # Will fail, hour value is one over upper boundary
        assert (     schedule.isActive ( StringToEpochTime ( '200506021029' ) ) );    # Will work
        assert ( not schedule.isActive ( StringToEpochTime ( '200506011029' ) ) );    # Will fail, day of month value is one less than lower boundary
        assert (     schedule.isActive ( StringToEpochTime ( '200506241029' ) ) );    # Will work
        assert ( not schedule.isActive ( StringToEpochTime ( '200506251029' ) ) );    # Will fail, day of week is one over upper bounday

    # Test catch-up schedule
    schedule = CronSchedule ( '5,10 12, * *' );
    assert (     schedule.isActive ( StringToEpochTime ( '200501011205' ) ) );                                          # Will work
    assert ( not schedule.isActive ( StringToEpochTime ( '200501011105' ) ) );                                          # Will fail, wrong hour
    assert (     schedule.isActive ( StringToEpochTime ( '200501011205' ), StringToEpochTime ( '200501011204' ) ) );    # Will work, 1205 is a match
    assert (     schedule.isActive ( StringToEpochTime ( '200501011206' ), StringToEpochTime ( '200501011204' ) ) );    # Will work, 1205 falls in the middle of the range
    assert ( not schedule.isActive ( StringToEpochTime ( '200501011206' ), StringToEpochTime ( '200501011205' ) ) );    # Will fail, 1205 was the last run time and is not in the range
    assert (     schedule.isActive ( StringToEpochTime ( '200501020000' ), StringToEpochTime ( '200501010000' ) ) );    # Will work, both times fall in the range
    assert (     schedule.isActive ( StringToEpochTime ( '200501011210' ), StringToEpochTime ( '200501011200' ) ) );    # Will work, 1210 is a match
    assert (     schedule.isActive ( StringToEpochTime ( '200501011210' ), StringToEpochTime ( '200501011205' ) ) );    # Will work, 1210 is a match though 1205 was the last time and so isn't hit
    assert ( not schedule.isActive ( StringToEpochTime ( '200501011209' ), StringToEpochTime ( '200501011205' ) ) );    # Will fail, 1205 and 1210 are on either side of the range
 
