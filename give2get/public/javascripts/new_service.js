    $(document).ready(function() {
       
        $("#cBoxAddDate").click(handleDates);
        $("#cBoxAddLocation").click(handleLocation);
        $("#cBoxPeriodicalInfo").click(handlePeriodicalInfo);

        handleDates();
        handleLocation();
        handlePeriodicalInfo();

    });

    function handleDates() {

        if (  $('#cBoxAddDate').attr('checked') ) {

            $('#div_startdate').find('*').attr('disabled', false);
            $('#div_enddate').find('*').attr('disabled', false);            

        } else {

            $('#div_startdate').find('*').attr('disabled', true);
            $('#div_enddate').find('*').attr('disabled', true);


            $("#postStartDate2").text("");
            $('#postEndDate2').text("");

        }

    }

    function handleLocation () {

        if (  $('#cBoxAddLocation').attr('checked') ) {

            $('#div_sehir').find('*').attr('disabled', false);
            $('#div_ilce').find('*').attr('disabled', false);
            $('#div_semt').find('*').attr('disabled', false);

            fetchSehirler();

        } else {

            $('#div_sehir').find('*').attr('disabled', true);
            $('#div_ilce').find('*').attr('disabled', true);
            $('#div_semt').find('*').attr('disabled', true);

            $("#sehirler").empty();
            $("#ilceler").empty();
            $("#semtler").empty();

        }

    }

    function handlePeriodicalInfo() {

        if ( $('#cBoxPeriodicalInfo').attr('checked') ) {

            $('#div_fromDay').find('*').attr('disabled', false);
            $('#div_toDay').find('*').attr('disabled', false);
            $('#div_between_from').find('*').attr('disabled', false);
            $('#div_between_to').find('*').attr('disabled', false);

        } else {

            $('#div_fromDay').find('*').attr('disabled', true);
            $('#div_toDay').find('*').attr('disabled', true);
            $('#div_between_from').find('*').attr('disabled', true);
            $('#div_between_to').find('*').attr('disabled', true);

            $('#div_fromDay.select').val('-');
            

            
        }

    }

    function fetchSehirler() {

        $("#sehirler").empty();

        $.getJSON("@{Services.getSehirler()}", function(sehirler) {

            var index = 0;

            $('#sehirler').append("<option>-</option>");

            $.each(sehirler, function(id, name) {

                $('#sehirler').append("<option value='" + sehirler[index].id + "'>" + sehirler[index].name + "</option>");

                index++;

            });

        });

    }

    function fetchIlceler() {

        var selectedSehirId = $("#sehirler").val();
       
        $("#ilceler").empty();
                
        $.getJSON("@{Services.getIlceler()}", {sehirId : selectedSehirId}, function(ilceler) {

            var index = 0;

            $('#ilceler').append("<option>-</option>");

            $.each(ilceler, function(id, name) {

                $('#ilceler').append("<option value='" + ilceler[index].id + "'>" + ilceler[index].name + "</option>");

                index++;

            });

        });

    }

    function fetchSemtler() {

        var selectedIlceId = $("#ilceler").val();

        $("#semtler").empty();
        
        $.getJSON("@{Services.getSemtler()}", {ilceId: selectedIlceId}, function(semtler) {

            var index = 0;

            $('#semtler').append("<option>-</option>");

            $.each(semtler, function(id, name) {

                $('#semtler').append("<option value='" + semtler[index].id + "'>" + semtler[index].name + "</option>");

                index++;

            });

        });

    }



