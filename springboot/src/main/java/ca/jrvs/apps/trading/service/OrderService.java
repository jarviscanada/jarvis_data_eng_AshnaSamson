package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.PositionDao;
import ca.jrvs.apps.trading.dao.QuoteDao;
import ca.jrvs.apps.trading.dao.SecurityOrderDao;
import ca.jrvs.apps.trading.dto.MarketOrderDto;
import ca.jrvs.apps.trading.model.domain.Account;
import ca.jrvs.apps.trading.model.domain.Position;
import ca.jrvs.apps.trading.model.domain.Quote;
import ca.jrvs.apps.trading.model.domain.SecurityOrder;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;


@Service
@Transactional
public class OrderService {

    public static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final AccountDao accountDao;
    private final SecurityOrderDao securityOrderDao;
    private final QuoteDao quoteDao;
    private final PositionDao positionDao;

    @Autowired
    public OrderService(AccountDao accountDao, SecurityOrderDao securityOrderDao, QuoteDao quoteDao,
                        PositionDao positionDao) {
        this.accountDao = accountDao;
        this.securityOrderDao = securityOrderDao;
        this.quoteDao = quoteDao;
        this.positionDao = positionDao;
    }

    /**
     * Execute a market order
     *
     * @param orderDto - The market order to execute
     * @return SecurityOrder from security_order table
     * @throws DataAccessException      if unable to get data from the DAO
     * @throws IllegalArgumentException for invalid input
     */
    public SecurityOrder executeMarketOrder(MarketOrderDto orderDto) {
        if (orderDto == null || orderDto.getAccountId() == null || orderDto.getTicker() == null
                || orderDto.getSize() == null) {
            throw new IllegalArgumentException("ERROR: MarketOrderDto is null, or contains null values");
        }
        if (!quoteDao.existsById(orderDto.getTicker())) {
            throw new DataRetrievalFailureException(
                    "ERROR: Could not find quote with ticker " + orderDto.getTicker());
        }
        Account account;
        try {
            account = accountDao.findById(orderDto.getAccountId()).get();
        } catch (Exception ex) {
            throw new DataRetrievalFailureException(
                    "ERROR: Could not find account with ID " + orderDto.getAccountId());
        }
        SecurityOrder order = new SecurityOrder();
        order.setAccountId(orderDto.getAccountId());
        order.setStatus("IN PROGRESS");
        order.setTicker(orderDto.getTicker());
        order.setOrderSize(orderDto.getSize());
        order.setPrice(0.0);
        order = securityOrderDao.save(order);
        if (orderDto.getSize() > 0) {
            handleBuyMarketOrder(orderDto, order, account);
        } else if (orderDto.getSize() < 0) {
            handleSellMarketOrder(orderDto, order, account);
        } else {
            throw new IllegalArgumentException("ERROR: Order size is 0");
        }
        return securityOrderDao.findById(order.getId()).get();
    }

    /**
     * Helper method to execute a buy order
     *
     * @param marketOrderDto - User order
     * @param securityOrder  - To be saved into the database
     * @param account        - Account to withdraw funds from
     */
    protected void handleBuyMarketOrder(MarketOrderDto marketOrderDto, SecurityOrder securityOrder,
                                        Account account) {
        Quote quote = quoteDao.findById(marketOrderDto.getTicker()).get();
        securityOrder.setPrice(quote.getAskPrice());
        if (account.getAmount() < securityOrder.getOrderSize() * securityOrder.getPrice()) {
            securityOrder.setStatus("CANCELLED");
            securityOrder.setNotes(
                    "Insufficient funds. Order total: " + securityOrder.getOrderSize() * securityOrder.getPrice());
        } else {
            account.setAmount(account.getAmount() - securityOrder.getOrderSize() * securityOrder.getPrice());
            securityOrder.setStatus("FILLED");
            accountDao.save(account);
        }
        securityOrderDao.save(securityOrder);
    }

    /**
     * Helper method to execute a sell order
     *
     * @param marketOrderDto - User order
     * @param securityOrder  - To be saved into the database
     * @param account        - Account to deposit funds into
     */
    protected void handleSellMarketOrder(MarketOrderDto marketOrderDto, SecurityOrder securityOrder,
                                         Account account) {
        Quote quote = quoteDao.findById(marketOrderDto.getTicker()).get();
        securityOrder.setPrice(quote.getBidPrice());
        Position position = positionDao.findById(marketOrderDto.getAccountId(), marketOrderDto.getTicker()).get();
        if (-securityOrder.getOrderSize() > position.getPosition()) {
            securityOrder.setStatus("CANCELLED");
            securityOrder.setNotes(
                    "Insufficient position. Current position: " + position.getPosition());
        } else {
            account.setAmount(account.getAmount() + (-securityOrder.getOrderSize()) * securityOrder.getPrice());
            securityOrder.setStatus("FILLED");
            accountDao.save(account);
        }
        securityOrderDao.save(securityOrder);
    }
}
