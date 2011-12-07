var test = ({
    total : 4,
    _0 : {
        type : "scaffold",
        user : "Bob",
        submitted : "4:50pm today",
        subtasks : 100,
        ok : 40,
        failures: 2,
        warnings :4,
        running : 5,
        heartbeat : "Ok",
        details : {
            total: 3,
            _0 : {
                type : "subtask",
                title : "Mascot search of \"File1.RAW\"",
                status : "completed",
                endtime : "4:55pm"
            },
            _1 : {
                type : "subtask",
                title : "Mascot search of \"File2.RAW\"",
                status : "failed"
            },
            _2 : {
                type : "subtask",
                title : "Mascot search of \"Blah.RAW\"",
                status : "warnings",
                warnings :
                {
                    total: 5,
                    _0 : {
                        type : "warning",
                        message : "Missing import directive",
                        severity : "low"
                    },
                    _1 : {
                        type : "warning",
                        message : "Too many tasks operating simultaneously",
                        severity : "medium"
                    },
                    _2 : {
                        type : "warning",
                        message : "The system is running out of memory",
                        severity : "high"
                    }
                }
            }
        }
    }  ,
    _1 : {
        type : "scaffold",
        user : "Harry1",
        submitted : "10:00am yesteday",
        subtasks : 67,
        ok : 10,
        failures: 10,
        completed : "11:00am today",
        expanded : true,
        details : { total: 0 }
    },
    _2 : {
        type : "scaffold",
        user : "Harry2",
        submitted : "10:00am yesteday",
        subtasks : 67,
        ok : 67,
        completed : "11:00am today",
        results : "http://www.google.com",
        details : { total: 0 }
    },
    _3 : {
        type : "other",
        user : "Ken",
        submitted : "6:00am today",
        subtasks : 67,
        ok : 47,
        failures: 20,
        completed : "11:00am today",
        results : "http://www.google.com",
        details : { total: 0 }
    }

});