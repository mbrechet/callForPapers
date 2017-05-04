/*
 * Copyright (c) 2016 BreizhCamp
 * [http://breizhcamp.org]
 *
 * This file is part of CFP.io.
 *
 * CFP.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.cfp.api;

import io.cfp.entity.Role;
import io.cfp.mapper.ProposalMapper;
import io.cfp.model.Proposal;
import io.cfp.model.User;
import io.cfp.model.queries.ProposalQuery;
import io.cfp.multitenant.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = { "/v1" }, produces = APPLICATION_JSON_UTF8_VALUE)
public class ProposalsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProposalsController.class);

    @Autowired
    private ProposalMapper proposals;


    @GetMapping("/proposals")
    @Secured({Role.REVIEWER, Role.ADMIN})
    public List<Proposal> search(@TenantId String event,
                                 @RequestParam(name = "state", required = false) String state,
                                 @RequestParam(name = "userId", required = false) Integer userId) {
        ProposalQuery query = new ProposalQuery()
            .setEventId(event)
            .setState(state)
            .setUserId(userId);

        LOGGER.info("Search Proposals : {}", query);
        List<Proposal> p = proposals.findAll(query);
        LOGGER.debug("Found {} Proposals", p.size());
        return p;
    }

    @GetMapping("/proposals/{id}")
    public Proposal get(@PathVariable Integer id) {
        LOGGER.info("Get Proposal with id {}", id);
        Proposal proposal = proposals.findById(id);
        LOGGER.debug("Found Proposal {}", proposal);
        return proposal;
    }

    @PostMapping("/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    @Secured(io.cfp.entity.Role.AUTHENTICATED)
    public Proposal create(@TenantId String event,
                           Proposal proposal) {
        LOGGER.info("Create a Proposal : {}", proposal.getName());
        proposals.insert(proposal.setEventId(event));

        return proposal;
    }

    @PutMapping("/proposals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured(io.cfp.entity.Role.AUTHENTICATED)
    public void update(@AuthenticationPrincipal User user,
                       @TenantId String event,
                       @PathVariable Integer id, Proposal proposal) {

        if (!user.hasRole(Role.ADMIN)) {
            // FIXME only ADMIN can set state to ACCEPTED/REJECTED
            // FIXME simple user can only update his own proposal
        }
        proposal.setId(id);
        LOGGER.info("Update a Proposal : {}", proposal.getName());
        proposals.updateForEvent(proposal, event);
    }

    @DeleteMapping("/proposals/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Secured(io.cfp.entity.Role.ADMIN)
    public void delete(@AuthenticationPrincipal User user,
                       @TenantId String event,
                       @PathVariable Integer id) {
        LOGGER.info("Delete a Proposal with id {}", id);
        proposals.deleteForEvent(id, event);
    }

}
