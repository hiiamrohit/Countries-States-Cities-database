<?php
require_once("dbconfig.php");
class location extends dbconfig {
   
   public static $data;

   function __construct() {
     parent::__construct();
   }
 
 // Fetch all countries list
   public static function getCountries() {
     try {
       $query = "SELECT id, name FROM crm_countries";
       $result = dbconfig::run($query);
       if(!$result) {
         throw new exception("Country not found.");
       }
       while($resultSet = mysqli_fetch_assoc($result)) {
        $res[$resultSet['id']] = $resultSet['name'];
       }
       $data = array('status'=>'success', 'tp'=>1, 'msg'=>"Countries fetched successfully.", 'result'=>$res);
     } catch (Exception $e) {
       $data = array('status'=>'error', 'tp'=>0, 'msg'=>$e->getMessage);
     } finally {
        return $data;
     }
   }

  // Fetch all states list by country id
  public static function getStates($countryId) {
     try {
       $query = "SELECT id, name FROM crm_states WHERE country_id=".$countryId;
       $result = dbconfig::run($query);
       if(!$result) {
         throw new exception("State not found.");
       }
       while($resultSet = mysqli_fetch_assoc($result)) {
        $res[$resultSet['id']] = $resultSet['name'];
       }
       $data = array('status'=>'success', 'tp'=>1, 'msg'=>"States fetched successfully.", 'result'=>$res);
     } catch (Exception $e) {
       $data = array('status'=>'error', 'tp'=>0, 'msg'=>$e->getMessage);
     } finally {
        return $data;
     }
   }

 // Fetch all cities list by state id
  public static function getCities($stateId) {
     try {
       $query = "SELECT id, name FROM crm_states WHERE state_id=".$stateId;
       $result = dbconfig::run($query);
       if(!$result) {
         throw new exception("City not found.");
       }
       while($resultSet = mysqli_fetch_assoc($result)) {
        $res[$resultSet['id']] = $resultSet['name'];
       }
       $data = array('status'=>'success', 'tp'=>1, 'msg'=>"Cities fetched successfully.", 'result'=>$res);
     } catch (Exception $e) {
       $data = array('status'=>'error', 'tp'=>0, 'msg'=>$e->getMessage);
     } finally {
        return $data;
     }
   }   


}
