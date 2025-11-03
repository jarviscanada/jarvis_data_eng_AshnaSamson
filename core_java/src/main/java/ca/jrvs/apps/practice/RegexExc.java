package ca.jrvs.apps.practice;

public interface RegexExc {

    /**
     * Return true if filename extension is jpg or jpeg (Case insensitive)
     *
     * @param filename
     * @return
     */
    public boolean matchJpeg(String filename);

    /**
     * Return true if IP is valid To simplify the problem, IP addres range is from 0.0.0.0 to
     * 999.999.999.999
     *
     * @param ip
     * @return
     */
    public boolean matchIp(String ip);

    /**
     * Return true if line is empty (e.g. empty char, white space, tabs, etc.)
     *
     * @param line
     * @return
     */
    public boolean isEmptyLine(String line);
}