package ca.jrvs.apps.trading.service;

import ca.jrvs.apps.trading.dao.AccountDao;
import ca.jrvs.apps.trading.dao.PositionDao;
import ca.jrvs.apps.trading.dao.SecurityOrderDao;
import ca.jrvs.apps.trading.dao.TraderDao;
import ca.jrvs.apps.trading.model.domain.Account;
import ca.jrvs.apps.trading.model.domain.SecurityOrder;
import ca.jrvs.apps.trading.model.domain.Trader;
import ca.jrvs.apps.trading.model.view.TraderAccountView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TraderAccountService {

    private final TraderDao traderDao;
    private final AccountDao accountDao;
    private final PositionDao positionDao;
    private final SecurityOrderDao securityOrderDao;

    @Autowired
    public TraderAccountService(TraderDao traderDao, AccountDao accountDao, PositionDao positionDao,
                                SecurityOrderDao securityOrderDao) {
        this.traderDao = traderDao;
        this.accountDao = accountDao;
        this.positionDao = positionDao;
        this.securityOrderDao = securityOrderDao;
    }

    /**
     * Create a new trader an initialize a new account with 0 amount.
     *
     * @param trader - Cannot be null, and all fields except ID must not be null
     * @return a TraderAccountView
     * @throws IllegalArgumentException if the trader has null fields or is null itself
     */
    public TraderAccountView createTraderAndAccount(Trader trader) {
        if (trader == null || trader.getFirstName() == null || trader.getLastName() == null
                || trader.getDob() == null || trader.getCountry() == null || trader.getEmail() == null) {
            throw new IllegalArgumentException("ERROR: Trader is null or contains null values");
        }
        Trader savedTrader = traderDao.save(trader);
        Account newAccount = new Account();
        newAccount.setTraderId(savedTrader.getId());
        newAccount.setAmount(0.0);
        Account savedAccount = accountDao.save(newAccount);
        return new TraderAccountView(savedTrader, savedAccount);
    }

    /**
     * Delete a trader if it has no open positions and 0 cash balance
     *
     * @param traderId - ID of trader to delete, must not be null
     * @throws IllegalArgumentException if tradeId is null, trader is not found OR if trader cannot be
     *                                  deleted
     */
    public void deleteTraderById(Integer traderId) {
        if (traderId == null) {
            throw new IllegalArgumentException("ERROR: traderId is null");
        }
        if (!traderDao.existsById(traderId)) {
            throw new IllegalArgumentException(
                    "ERROR: Trader with id " + traderId + " could not be found");
        }
        Account traderAccount = accountDao.findByTraderId(traderId).get();
        if (traderAccount.getAmount() > 0) {
            throw new IllegalArgumentException(
                    "ERROR: Trader with id " + traderId + "'s account balance is not 0");
        }
        if (positionDao.findAllByAccountId(traderAccount.getId()).size() > 0) {
            throw new IllegalArgumentException(
                    "ERROR: Trader with id " + traderId + " has open positions");
        }
        List<SecurityOrder> orderList = securityOrderDao.findAllByAccountId(traderAccount.getId());
        orderList.forEach(o -> securityOrderDao.deleteById(o.getId()));
        accountDao.deleteById(traderAccount.getId());
        traderDao.deleteById(traderId);
    }

    /**
     * Deposit the funds into the account belonging to the trader with the given traderId
     *
     * @param traderId - ID of the trader whose account to deposit the funds to, must not be null
     * @param funds    - Amount of funds to deposit, must be greater than 0
     * @return the updated Account
     * @throws IllegalArgumentException if traderId is null, a trader with the given ID cannot be
     *                                  found, or funds <= 0
     */
    public Account deposit(Integer traderId, Double funds) {
        if (funds <= 0) {
            throw new IllegalArgumentException("ERROR: funds must be positive");
        }
        if (traderId == null) {
            throw new IllegalArgumentException("ERROR: traderId is null");
        }
        if (!traderDao.existsById(traderId)) {
            throw new IllegalArgumentException(
                    "ERROR: Trader with id " + traderId + " could not be found");
        }
        Account traderAccount = accountDao.findByTraderId(traderId).get();
        traderAccount.setAmount(traderAccount.getAmount() + funds);
        return accountDao.save(traderAccount);
    }

    /**
     * Withdraw the funds from the account belonging to the trader with the given traderId
     *
     * @param traderId - ID of the trader whose account to deposit the funds to, must not be null
     * @param funds    - Amount of funds to withdraw, must be greater than zero and not exceed the
     *                 amount in the account
     * @return the updated Account
     * @throws IllegalArgumentException if traderId is null, a trader with the given ID cannot be
     *                                  found, funds <= 0 or funds is larger than the account's
     *                                  amount
     */
    public Account withdraw(Integer traderId, Double funds) {
        if (funds <= 0) {
            throw new IllegalArgumentException("ERROR: funds must be positive");
        }
        if (traderId == null) {
            throw new IllegalArgumentException("ERROR: traderId is null");
        }
        if (!traderDao.existsById(traderId)) {
            throw new IllegalArgumentException(
                    "ERROR: Trader with id " + traderId + " could not be found");
        }
        Account traderAccount = accountDao.findByTraderId(traderId).get();
        if (funds > traderAccount.getAmount()) {
            throw new IllegalArgumentException("ERROR: Trying to withdraw more funds than are in the account");
        }
        traderAccount.setAmount(traderAccount.getAmount() - funds);
        return accountDao.save(traderAccount);
    }
}