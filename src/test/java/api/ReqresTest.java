package api;

import org.junit.Assert;
import org.junit.Test;

import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;


public class ReqresTest {
    private final static String URL = "https://reqres.in";

    //GET ЗАПРОС
    @Test()
    public void checkAvatarAndIdTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOk200());
        List<UserData> user = given()
                .when()
                .get("/api/users?page=2")
                .then().log().all()
                .extract().body().jsonPath().getList("data", UserData.class);

        for (UserData x : user) {
            Assert.assertTrue(x.getAvatar().contains(x.getId().toString()));
        }

        Assert.assertTrue(user.stream().allMatch(x -> x.getEmail().endsWith("@reqres.in")));

        List<String> avatars = user.stream().map(UserData::getAvatar).collect(Collectors.toList());
        List<String> ids = user.stream().map(x -> x.getId().toString()).collect(Collectors.toList());

        for (int i = 0; i < avatars.size(); i++) {
            Assert.assertTrue(avatars.get(i).contains(ids.get(i)));
        }
    }

    //POST ЗАПРОС
    @Test
    public void successRegTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOk200());
        Integer id = 4;
        String token = "QpwL5tke4Pnpja7X4";
        Register user = new Register("eve.holt@reqres.in", "pistol");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("/api/register")
                .then().log().all()
                .extract().as(SuccessReg.class);
        Assert.assertEquals(id, successReg.getId());
        Assert.assertEquals(token, successReg.getToken());

    }

    @Test
    public void unSuccessRegTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecError400());
        Register user = new Register("sydney@fife", "");
        UnSeccessReg unSeccessReg = given()
                .body(user)
                .post("/api/register")
                .then().log().all()
                .extract().as(UnSeccessReg.class);
        Assert.assertEquals("Missing password", unSeccessReg.getError());
    }

    @Test
    public void sortedYears() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecOk200());
        List<ColorsData> colors = given()
                .when()
                .get("/api/unknown")
                .then().log().all()
                .extract().body().jsonPath().getList("data", ColorsData.class);
        List<Integer> years = colors.stream().map(ColorsData::getYear).collect(Collectors.toList());
        List<Integer> sortedYears = years.stream().sorted().collect(Collectors.toList());

        Assert.assertEquals(sortedYears, years);
    }

    //DELETE ЗАПРОС
    @Test
    public void deleteUserTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(204));
        given()
                .when()
                .delete("/api/users/2")
                .then().log().all();
    }

    //PUT ЗАПРОС
    @Test
    public void timeTest() {
        Specifications.installSpecification(Specifications.requestSpec(URL), Specifications.responseSpecUnique(200));
        UserTime user = new UserTime("morpheus", "zion resident");
        UserTimeResponse response = given()
                .body(user)
                .when()
                .put("/api/users/2")
                .then().log().all()
                .extract().as(UserTimeResponse.class);

        String regex = "(.{5})$";
        String currentTime = Clock.systemUTC().instant().toString().replaceAll(regex,"");
        Assert.assertEquals(currentTime, response.getUpdatedAt().replaceAll(regex,""));
        System.out.println(currentTime);
        System.out.println(response.getUpdatedAt().replaceAll(regex,""));
    }


}
