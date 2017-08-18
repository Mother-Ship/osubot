package top.mothership.osubot.pojo;

public class User {
    private String username;
    private Integer user_id;
    private Integer count300;
    private Integer count100;
    private Integer count50;
    private Integer playcount;
    private Float accuracy;
    private Float pp_raw;
    private Long ranked_score;
    private Long total_score;
    private Float level;
    private Integer pp_rank;
    private Integer count_rank_ss;
    private Integer count_rank_s;
    private Integer count_rank_a;

    public Integer getCount_rank_ss() {
        return count_rank_ss;
    }

    public void setCount_rank_ss(Integer count_rank_ss) {
        this.count_rank_ss = count_rank_ss;
    }

    public Integer getCount_rank_s() {
        return count_rank_s;
    }

    public void setCount_rank_s(Integer count_rank_s) {
        this.count_rank_s = count_rank_s;
    }

    public Integer getCount_rank_a() {
        return count_rank_a;
    }

    public void setCount_rank_a(Integer count_rank_a) {
        this.count_rank_a = count_rank_a;
    }

    public Integer getCount300() {
        return count300;
    }

    public void setCount300(Integer count300) {
        this.count300 = count300;
    }

    public Integer getCount100() {
        return count100;
    }

    public void setCount100(Integer count100) {
        this.count100 = count100;
    }

    public Integer getCount50() {
        return count50;
    }

    public void setCount50(Integer count50) {
        this.count50 = count50;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getPlaycount() {
        return playcount;
    }

    public void setPlaycount(Integer playcount) {
        this.playcount = playcount;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public Float getPp_raw() {
        return pp_raw;
    }

    public void setPp_raw(Float pp_raw) {
        this.pp_raw = pp_raw;
    }

    public Long getRanked_score() {
        return ranked_score;
    }

    public void setRanked_score(Long ranked_score) {
        this.ranked_score = ranked_score;
    }

    public Long getTotal_score() {
        return total_score;
    }

    public void setTotal_score(Long total_score) {
        this.total_score = total_score;
    }

    public Float getLevel() {
        return level;
    }

    public void setLevel(Float level) {
        this.level = level;
    }

    public Integer getPp_rank() {
        return pp_rank;
    }

    public void setPp_rank(Integer pp_rank) {
        this.pp_rank = pp_rank;
    }
}
